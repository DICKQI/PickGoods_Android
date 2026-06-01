package com.pickgoods.app.data.network

import com.pickgoods.app.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

private const val PLACEHOLDER_HOST = "pickgoods.local"

@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url.toString()

        if (PLACEHOLDER_HOST in url) {
            val baseUrl = runBlocking { tokenManager.getBaseUrl() }.trimEnd('/')
            val newUrl = url.replace("http://$PLACEHOLDER_HOST/", "$baseUrl/")
            val newRequest = originalRequest.newBuilder()
                .url(newUrl)
                .build()
            return chain.proceed(newRequest)
        }

        return chain.proceed(originalRequest)
    }
}
