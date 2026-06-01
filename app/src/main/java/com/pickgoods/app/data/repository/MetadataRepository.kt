package com.pickgoods.app.data.repository

import android.util.Log
import com.pickgoods.app.data.api.MetadataApi
import com.pickgoods.app.data.model.BgmCreateCharacterItem
import com.pickgoods.app.data.model.BgmCreateCharactersRequest
import com.pickgoods.app.data.model.BgmCreateCharactersResponse
import com.pickgoods.app.data.model.BgmGetCharactersRequest
import com.pickgoods.app.data.model.BgmGetCharactersResponse
import com.pickgoods.app.data.model.BgmSearchSubjectsRequest
import com.pickgoods.app.data.model.BgmSearchSubjectsResponse
import com.pickgoods.app.data.model.Category
import com.pickgoods.app.data.model.CategoryRequest
import com.pickgoods.app.data.model.Character
import com.pickgoods.app.data.model.CharacterRequest
import com.pickgoods.app.data.model.IP
import com.pickgoods.app.data.model.IPRequest
import com.pickgoods.app.data.model.Theme
import com.pickgoods.app.data.model.ThemeRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataRepository @Inject constructor(
    private val metadataApi: MetadataApi
) {
    suspend fun getIPs(search: String? = null): GoodsResult<List<IP>> = safeCall {
        metadataApi.getIPs(search = search).toGoodsResult()
    }

    suspend fun createIP(request: IPRequest): GoodsResult<IP> = safeCall {
        metadataApi.createIP(request).toGoodsResult()
    }

    suspend fun updateIP(id: Int, request: IPRequest): GoodsResult<IP> = safeCall {
        metadataApi.updateIP(id, request).toGoodsResult()
    }

    suspend fun deleteIP(id: Int): GoodsResult<Unit> = safeCall {
        metadataApi.deleteIP(id).toUnitGoodsResult()
    }

    suspend fun getCharacters(search: String? = null, ip: Int? = null): GoodsResult<List<Character>> = safeCall {
        metadataApi.getCharacters(search = search, ip = ip).toGoodsResult()
    }

    suspend fun createCharacter(request: CharacterRequest): GoodsResult<Character> = safeCall {
        metadataApi.createCharacter(request).toGoodsResult()
    }

    suspend fun updateCharacter(id: Int, request: CharacterRequest): GoodsResult<Character> = safeCall {
        metadataApi.updateCharacter(id, request).toGoodsResult()
    }

    suspend fun deleteCharacter(id: Int): GoodsResult<Unit> = safeCall {
        metadataApi.deleteCharacter(id).toUnitGoodsResult()
    }

    suspend fun getCategories(search: String? = null): GoodsResult<List<Category>> = safeCall {
        metadataApi.getCategories(search = search).toGoodsResult()
    }

    suspend fun createCategory(request: CategoryRequest): GoodsResult<Category> = safeCall {
        metadataApi.createCategory(request).toGoodsResult()
    }

    suspend fun patchCategory(id: Int, request: CategoryRequest): GoodsResult<Category> = safeCall {
        metadataApi.patchCategory(id, request).toGoodsResult()
    }

    suspend fun deleteCategory(id: Int): GoodsResult<Unit> = safeCall {
        metadataApi.deleteCategory(id).toUnitGoodsResult()
    }

    suspend fun getThemes(search: String? = null): GoodsResult<List<Theme>> = safeCall {
        metadataApi.getThemes(search = search).toGoodsResult()
    }

    suspend fun getThemeDetail(id: Int): GoodsResult<Theme> = safeCall {
        metadataApi.getThemeDetail(id).toGoodsResult()
    }

    suspend fun createTheme(request: ThemeRequest): GoodsResult<Theme> = safeCall {
        metadataApi.createTheme(request).toGoodsResult()
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

    private fun File.toImagePart(fieldName: String): MultipartBody.Part {
        val body = asRequestBody("image/jpeg".toMediaType())
        return MultipartBody.Part.createFormData(fieldName, name, body)
    }
}
