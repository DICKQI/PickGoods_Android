package com.pickgoods.app.di

import com.pickgoods.app.data.api.AuthApi
import com.pickgoods.app.data.api.AdminApi
import com.pickgoods.app.data.api.GoodsApi
import com.pickgoods.app.data.api.LocationApi
import com.pickgoods.app.data.api.MetadataApi
import com.pickgoods.app.data.api.ShowcaseApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAdminApi(retrofit: Retrofit): AdminApi = retrofit.create(AdminApi::class.java)

    @Provides
    @Singleton
    fun provideGoodsApi(retrofit: Retrofit): GoodsApi = retrofit.create(GoodsApi::class.java)

    @Provides
    @Singleton
    fun provideMetadataApi(retrofit: Retrofit): MetadataApi = retrofit.create(MetadataApi::class.java)

    @Provides
    @Singleton
    fun provideLocationApi(retrofit: Retrofit): LocationApi = retrofit.create(LocationApi::class.java)

    @Provides
    @Singleton
    fun provideShowcaseApi(retrofit: Retrofit): ShowcaseApi = retrofit.create(ShowcaseApi::class.java)
}
