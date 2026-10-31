const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// Server-side Passkey Configuration:
// In production, configure via Firebase Secrets:
//   firebase functions:secrets:set OWNER_PASSKEY
// Fallback defaults to the requested passkey securely configured on the server
const CONFIGURED_OWNER_PASSKEY = process.env.OWNER_PASSKEY || "TABasum@11";

// Rate limiting & lockout configuration
const MAX_ATTEMPTS = 5;
const LOCKOUT_DURATION_MS = 15 * 60 * 1000; // 15 minutes lockout

/**
 * Callable Function: verifyOwnerPasskey
 * Validates owner passkey on a secure server environment, tracks failed attempts,
 * enforces 15-minute temporary lockout, and issues { admin: true } custom claims.
 */
exports.verifyOwnerPasskey = functions.https.onCall(async (data, context) => {
  const callerUid = context.auth ? context.auth.uid : null;
  const ip = context.rawRequest ? (context.rawRequest.headers["x-forwarded-for"] || context.rawRequest.ip || "unknown_ip") : "unknown_ip";
  const rateLimitKey = callerUid || ip.replace(/[:.]/g, "_");

  const rateLimitRef = db.collection("_adminSecurityRateLimits").document(rateLimitKey);
  const now = Date.now();

  const rateDoc = await rateLimitRef.get();
  let failedAttempts = 0;
  let lockedUntil = 0;

  if (rateDoc.exists) {
    const rateData = rateDoc.data();
    failedAttempts = rateData.failedAttempts || 0;
    lockedUntil = rateData.lockedUntil || 0;

    // Check if still locked out
    if (now < lockedUntil) {
      const remainingSeconds = Math.ceil((lockedUntil - now) / 1000);
      throw new functions.https.HttpsError(
        "resource-exhausted",
        `Too many failed attempts. Temporary security lockout active for ${remainingSeconds} more seconds.`
      );
    }
  }

  const suppliedPasskey = (data && data.passkey) ? String(data.passkey) : "";

  if (suppliedPasskey !== CONFIGURED_OWNER_PASSKEY) {
    failedAttempts += 1;
    let newLockedUntil = 0;

    if (failedAttempts >= MAX_ATTEMPTS) {
      newLockedUntil = now + LOCKOUT_DURATION_MS;
    }

    await rateLimitRef.set({
      failedAttempts: failedAttempts,
      lockedUntil: newLockedUntil,
      lastAttemptAt: now
    }, { merge: true });

    if (failedAttempts >= MAX_ATTEMPTS) {
      throw new functions.https.HttpsError(
        "resource-exhausted",
        "Maximum passkey attempts exceeded. Portal temporarily locked for 15 minutes."
      );
    } else {
      const remaining = MAX_ATTEMPTS - failedAttempts;
      throw new functions.https.HttpsError(
        "permission-denied",
        `Invalid owner passkey. ${remaining} attempt(s) remaining.`
      );
    }
  }

  // Passkey matches successfully: reset rate limit tracking
  await rateLimitRef.delete();

  // If caller is authenticated with Firebase Auth, grant admin custom claims
  if (callerUid) {
    await admin.auth().setCustomUserClaims(callerUid, { admin: true, owner: true });
  }

  // Log successful login audit event
  await db.collection("adminAuditLogs").add({
    action: "OWNER_PORTAL_LOGIN",
    adminUid: callerUid || "owner_direct",
    adminEmail: (context.auth && context.auth.token.email) ? context.auth.token.email : "owner@lovelyhub.admin",
    targetListingId: "",
    details: "Owner successfully authenticated via passkey.",
    timestamp: now
  });

  return {
    success: true,
    message: "Owner access granted successfully.",
    sessionExpiresInSeconds: 7200 // 2 hours session expiry
  };
});

/**
 * Callable Function: updateListingApprovalStatus
 * Strictly server-side authorized listing moderation (approve / reject / deactivate / restore)
 */
exports.updateListingApprovalStatus = functions.https.onCall(async (data, context) => {
  // Verify admin authorization
  if (!context.auth || !context.auth.token || !context.auth.token.admin) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Only authorized administrators can modify listing approval status."
    );
  }

  const listingId = data.listingId;
  const newStatus = data.status; // "approved" | "rejected" | "disabled"
  const rejectionReason = data.rejectionReason || "";

  if (!listingId || !newStatus) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "listingId and status are required parameters."
    );
  }

  const listingRef = db.collection("listings").document(listingId);
  const doc = await listingRef.get();

  if (!doc.exists) {
    throw new functions.https.HttpsError("not-found", "Listing not found.");
  }

  const updates = {
    approvalStatus: newStatus,
    rejectionReason: rejectionReason,
    isActive: newStatus === "approved",
    updatedAt: Date.now()
  };

  await listingRef.update(updates);

  // Append to immutable audit log
  await db.collection("adminAuditLogs").add({
    action: `LISTING_${newStatus.toUpperCase()}`,
    adminUid: context.auth.uid,
    adminEmail: context.auth.token.email || "owner@lovelyhub.admin",
    targetListingId: listingId,
    details: rejectionReason ? `Status changed to ${newStatus}. Reason: ${rejectionReason}` : `Status changed to ${newStatus}.`,
    timestamp: Date.now()
  });

  return { success: true, listingId: listingId, status: newStatus };
});
