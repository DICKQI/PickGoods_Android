package com.pickgoods.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_cache")
data class CacheEntry(
    @PrimaryKey val key: String,
    val payload: String,
    val updatedAt: Long = System.currentTimeMillis()
)
