package com.pickgoods.app.di

import android.content.Context
import androidx.room.Room
import com.pickgoods.app.data.local.CacheDao
import com.pickgoods.app.data.local.PickGoodsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PickGoodsDatabase {
        return Room.databaseBuilder(
            context,
            PickGoodsDatabase::class.java,
            "pickgoods.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCacheDao(database: PickGoodsDatabase): CacheDao = database.cacheDao()
}
