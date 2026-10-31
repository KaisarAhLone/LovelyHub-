package com.example.lovelyhub.data.model

data class AdminStats(
    val totalRestaurants: Int = 0,
    val pendingRestaurantApprovals: Int = 0,
    val approvedRestaurants: Int = 0,
    val totalRentals: Int = 0,
    val totalRooms: Int = 0,
    val totalServices: Int = 0,
    val totalMarketplace: Int = 0,
    val totalJobs: Int = 0,
    val totalPendingApprovals: Int = 0,
    val totalMessageActivity: Int = 0,
    val totalCallActivity: Int = 0,
    val totalAuditLogs: Int = 0
)
