package com.pickgoods.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pickgoods.app.data.api.MetadataApi
import com.pickgoods.app.data.local.CacheDao
import com.pickgoods.app.data.local.CacheEntry
import com.pickgoods.app.data.model.BgmCreateCharacterItem
import com.pickgoods.app.data.model.BgmCreateCharactersRequest
import com.pickgoods.app.data.model.BgmCreateCharactersResponse
import com.pickgoods.app.data.model.BgmGetCharactersRequest
import com.pickgoods.app.data.model.BgmGetCharactersResponse
import com.pickgoods.app.data.model.BgmSearchCharactersRequest
import com.pickgoods.app.data.model.BgmSearchCharactersResponse
import com.pickgoods.app.data.model.BgmSearchSubjectsRequest
import com.pickgoods.app.data.model.BgmSearchSubjectsResponse
import com.pickgoods.app.data.model.Category
import com.pickgoods.app.data.model.CategoryBatchUpdateOrderRequest
import com.pickgoods.app.data.model.CategoryBatchUpdateOrderResponse
import com.pickgoods.app.data.model.CategoryRequest
import com.pickgoods.app.data.model.Character
import com.pickgoods.app.data.model.CharacterRequest
import com.pickgoods.app.data.model.IP
import com.pickgoods.app.data.model.IPBatchUpdateOrderRequest
import com.pickgoods.app.data.model.IPBatchUpdateOrderResponse
import com.pickgoods.app.data.model.IPRequest
import com.pickgoods.app.data.model.MetadataOrderItem
import com.pickgoods.app.data.model.Theme
import com.pickgoods.app.data.model.ThemeRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataRepository @Inject constructor(
    private val metadataApi: MetadataApi,
    private val cacheDao: CacheDao
) {
    private val gson = Gson()

    suspend fun getIPs(search: String? = null, subjectType: Int? = null): GoodsResult<List<IP>> = cachedList(
        key = cacheKey("ips", search, subjectType)
    ) {
        metadataApi.getIPs(search = search, subjectType = subjectType)
    }

    suspend fun createIP(request: IPRequest): GoodsResult<IP> = safeCall {
        metadataApi.createIP(request).toGoodsResult()
    }

    suspend fun getIPDetail(id: Int): GoodsResult<IP> = safeCall {
        metadataApi.getIPDetail(id).toGoodsResult()
    }

    suspend fun updateIP(id: Int, request: IPRequest): GoodsResult<IP> = safeCall {
        metadataApi.updateIP(id, request).toGoodsResult()
    }

    suspend fun patchIP(id: Int, request: IPRequest): GoodsResult<IP> = safeCall {
        metadataApi.patchIP(id, request).toGoodsResult()
    }

    suspend fun deleteIP(id: Int): GoodsResult<Unit> = safeCall {
        metadataApi.deleteIP(id).toUnitGoodsResult()
    }

    suspend fun batchUpdateIPOrder(items: List<MetadataOrderItem>): GoodsResult<IPBatchUpdateOrderResponse> = safeCall {
        metadataApi.batchUpdateIPOrder(IPBatchUpdateOrderRequest(items)).toGoodsResult("IP排序更新失败")
    }

    suspend fun getIPCharacters(id: Int): GoodsResult<List<Character>> = safeCall {
        metadataApi.getIPCharacters(id).toGoodsResult()
    }

    suspend fun getCharacters(search: String? = null, ip: Int? = null): GoodsResult<List<Character>> = cachedList(
        key = cacheKey("characters", search, ip)
    ) {
        metadataApi.getCharacters(search = search, ip = ip)
    }

    suspend fun createCharacter(request: CharacterRequest): GoodsResult<Character> = safeCall {
        metadataApi.createCharacter(request).toGoodsResult()
    }

    suspend fun getCharacterDetail(id: Int): GoodsResult<Character> = safeCall {
        metadataApi.getCharacterDetail(id).toGoodsResult()
    }

    suspend fun createCharacterWithAvatar(
        name: String,
        ipId: Int,
        gender: String,
        avatarFile: File
    ): GoodsResult<Character> = safeCall {
        metadataApi.createCharacterWithAvatar(
            name = name.toRequestBody("text/plain".toMediaType()),
            ipId = ipId.toString().toRequestBody("text/plain".toMediaType()),
            gender = gender.toRequestBody("text/plain".toMediaType()),
            avatar = avatarFile.toImagePart("avatar")
        ).toGoodsResult("角色头像上传失败")
    }

    suspend fun updateCharacter(id: Int, request: CharacterRequest): GoodsResult<Character> = safeCall {
        metadataApi.updateCharacter(id, request).toGoodsResult()
    }

    suspend fun patchCharacter(id: Int, request: CharacterRequest): GoodsResult<Character> = safeCall {
        metadataApi.patchCharacter(id, request).toGoodsResult()
    }

    suspend fun updateCharacterWithAvatar(
        id: Int,
        name: String,
        ipId: Int,
        gender: String,
        avatarFile: File
    ): GoodsResult<Character> = safeCall {
        metadataApi.updateCharacterWithAvatar(
            id = id,
            name = name.toRequestBody("text/plain".toMediaType()),
            ipId = ipId.toString().toRequestBody("text/plain".toMediaType()),
            gender = gender.toRequestBody("text/plain".toMediaType()),
            avatar = avatarFile.toImagePart("avatar")
        ).toGoodsResult("角色头像上传失败")
    }

    suspend fun patchCharacterWithAvatar(
        id: Int,
        name: String,
        ipId: Int,
        gender: String,
        avatarFile: File
    ): GoodsResult<Character> = safeCall {
        metadataApi.patchCharacterWithAvatar(
            id = id,
            name = name.toRequestBody("text/plain".toMediaType()),
            ipId = ipId.toString().toRequestBody("text/plain".toMediaType()),
            gender = gender.toRequestBody("text/plain".toMediaType()),
            avatar = avatarFile.toImagePart("avatar")
        ).toGoodsResult("角色头像上传失败")
    }

    suspend fun deleteCharacter(id: Int): GoodsResult<Unit> = safeCall {
        metadataApi.deleteCharacter(id).toUnitGoodsResult()
    }

    suspend fun getCategories(search: String? = null): GoodsResult<List<Category>> = cachedList(
        key = cacheKey("categories", search)
    ) {
        metadataApi.getCategories(search = search)
    }

    suspend fun getCategoryTree(): GoodsResult<List<Category>> = cachedList(
        key = cacheKey("category-tree")
    ) {
        metadataApi.getCategoryTree()
    }

    suspend fun createCategory(request: CategoryRequest): GoodsResult<Category> = safeCall {
        metadataApi.createCategory(request).toGoodsResult()
    }

    suspend fun getCategoryDetail(id: Int): GoodsResult<Category> = safeCall {
        metadataApi.getCategoryDetail(id).toGoodsResult()
    }

    suspend fun updateCategory(id: Int, request: CategoryRequest): GoodsResult<Category> = safeCall {
        metadataApi.updateCategory(id, request).toGoodsResult()
    }

    suspend fun patchCategory(id: Int, request: CategoryRequest): GoodsResult<Category> = safeCall {
        metadataApi.patchCategory(id, request).toGoodsResult()
    }

    suspend fun deleteCategory(id: Int): GoodsResult<Unit> = safeCall {
        metadataApi.deleteCategory(id).toUnitGoodsResult()
    }

    suspend fun batchUpdateCategoryOrder(
        items: List<MetadataOrderItem>
    ): GoodsResult<CategoryBatchUpdateOrderResponse> = safeCall {
        metadataApi.batchUpdateCategoryOrder(CategoryBatchUpdateOrderRequest(items)).toGoodsResult("品类排序更新失败")
    }

    suspend fun getThemes(search: String? = null): GoodsResult<List<Theme>> = cachedList(
        key = cacheKey("themes", search)
    ) {
        metadataApi.getThemes(search = search)
    }

    suspend fun getThemeDetail(id: Int): GoodsResult<Theme> = safeCall {
        metadataApi.getThemeDetail(id).toGoodsResult()
    }

    suspend fun createTheme(request: ThemeRequest): GoodsResult<Theme> = safeCall {
        metadataApi.createTheme(request).toGoodsResult()
    }

    suspend fun updateTheme(id: Int, request: ThemeRequest): GoodsResult<Theme> = safeCall {
        metadataApi.updateTheme(id, request).toGoodsResult()
    }

    suspend fun patchTheme(id: Int, request: ThemeRequest): GoodsResult<Theme> = safeCall {
        metadataApi.patchTheme(id, request).toGoodsResult()
    }

    suspend fun uploadThemeImages(
        id: Int,
        imageFiles: List<File>,
        label: String? = null
    ): GoodsResult<Theme> = safeCall {
        val parts = imageFiles.map { it.toImagePart("additional_photos") }
        val labelBody = label
            ?.takeIf { it.isNotBlank() }
            ?.toRequestBody("text/plain".toMediaType())
        metadataApi.uploadThemeImages(id, parts, labelBody).toGoodsResult("主题图片上传失败")
    }

    suspend fun updateThemeImageLabel(
        id: Int,
        photoIds: List<Int>,
        label: String
    ): GoodsResult<Theme> = safeCall {
        val photoIdBodies = photoIds.map { it.toString().toRequestBody("text/plain".toMediaType()) }
        val labelBody = label.toRequestBody("text/plain".toMediaType())
        metadataApi.updateThemeImageLabel(id, photoIdBodies, labelBody).toGoodsResult("图片标签更新失败")
    }

    suspend fun deleteThemeImage(id: Int, photoId: Int): GoodsResult<Theme> = safeCall {
        metadataApi.deleteThemeImage(id, photoId).toGoodsResult("主题图片删除失败")
    }

    suspend fun deleteThemeImages(id: Int, photoIds: List<Int>): GoodsResult<Theme> = safeCall {
        metadataApi.deleteThemeImages(id, photoIds.joinToString(",")).toGoodsResult("主题图片删除失败")
    }

    suspend fun deleteTheme(id: Int): GoodsResult<Unit> = safeCall {
        metadataApi.deleteTheme(id).toUnitGoodsResult()
    }

    suspend fun searchBgmSubjects(
        keyword: String,
        subjectType: Int? = null
    ): GoodsResult<BgmSearchSubjectsResponse> = safeCall {
        metadataApi.searchBgmSubjects(
            BgmSearchSubjectsRequest(
                keyword = keyword,
                subjectType = subjectType
            )
        ).toGoodsResult()
    }

    suspend fun searchBgmCharacters(
        ipName: String,
        subjectType: Int? = null
    ): GoodsResult<BgmSearchCharactersResponse> = safeCall {
        metadataApi.searchBgmCharacters(
            BgmSearchCharactersRequest(
                ipName = ipName,
                subjectType = subjectType
            )
        ).toGoodsResult()
    }

    suspend fun getBgmCharactersBySubjectId(subjectId: Int): GoodsResult<BgmGetCharactersResponse> = safeCall {
        metadataApi.getBgmCharactersBySubjectId(BgmGetCharactersRequest(subjectId)).toGoodsResult()
    }

    suspend fun createBgmCharacters(
        characters: List<BgmCreateCharacterItem>
    ): GoodsResult<BgmCreateCharactersResponse> = safeCall {
        metadataApi.createBgmCharacters(BgmCreateCharactersRequest(characters)).toGoodsResult()
    }

    private suspend inline fun <T> safeCall(block: suspend () -> GoodsResult<T>): GoodsResult<T> {
        return try {
            block()
        } catch (e: Exception) {
            Log.e("MetadataRepository", "API request failed", e)
            GoodsResult.Error("请求失败: ${e.message ?: "未知错误"}")
        }
    }

    private suspend inline fun <reified T> cachedList(
        key: String,
        crossinline networkCall: suspend () -> Response<List<T>>
    ): GoodsResult<List<T>> {
        return try {
            when (val result = networkCall().toGoodsResult()) {
                is GoodsResult.Success -> {
                    cacheDao.upsert(CacheEntry(key, gson.toJson(result.data)))
                    result
                }
                is GoodsResult.Error -> cachedListFallback<T>(key) ?: result
            }
        } catch (e: Exception) {
            Log.e("MetadataRepository", "cached list request failed", e)
            cachedListFallback<T>(key)
                ?: GoodsResult.Error("请求失败: ${e.message ?: "未知错误"}")
        }
    }

    private suspend inline fun <reified T> cachedListFallback(key: String): GoodsResult<List<T>>? {
        val entry = cacheDao.get(key) ?: return null
        return runCatching {
            val type = object : TypeToken<List<T>>() {}.type
            val list: List<T> = gson.fromJson(entry.payload, type)
            GoodsResult.Success(list)
        }.getOrNull()
    }

    private fun File.toImagePart(fieldName: String): MultipartBody.Part {
        val body = asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(fieldName, name, body)
    }

    private fun cacheKey(prefix: String, vararg parts: Any?): String {
        return buildString {
            append("metadata:")
            append(prefix)
            parts.forEach { part ->
                append(':')
                append(part?.toString()?.takeIf { it.isNotBlank() } ?: "_")
            }
        }
    }
}
