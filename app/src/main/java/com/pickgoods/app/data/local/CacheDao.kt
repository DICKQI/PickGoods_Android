package com.pickgoods.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CacheDao {
    @Query("SELECT * FROM api_cache WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): CacheEntry?

    @Upsert
    suspend fun upsert(entry: CacheEntry)

    @Query("DELETE FROM api_cache WHERE `key` = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM api_cache WHERE `key` LIKE :prefix || '%'")
    suspend fun deleteByPrefix(prefix: String)
}
