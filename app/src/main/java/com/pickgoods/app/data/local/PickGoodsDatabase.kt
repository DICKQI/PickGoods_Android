package com.pickgoods.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CacheEntry::class],
    version = 1,
    exportSchema = false
)
abstract class PickGoodsDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
}
