package com.pickgoods.app.data.model

import com.google.gson.annotations.SerializedName

// --- IP ---

data class IP(
    val id: Int,
    val name: String,
    @SerializedName("short_name") val shortName: String? = null,
    val keywords: List<IPKeyword>? = null,
    @SerializedName("subject_type") val subjectType: Int? = null,
    @SerializedName("character_count") val characterCount: Int? = null,
    val order: Int = 0
)

data class IPKeyword(
    val id: Int,
    val value: String
)

// --- Character ---

data class Character(
    val id: Int,
    val name: String,
    val ip: IP,
    @SerializedName("ip_id") val ipId: Int? = null,
    val avatar: String? = null,
    val gender: String? = null
)

// --- Category ---

data class Category(
    val id: Int,
    val name: String,
    val parent: Int? = null,
    @SerializedName("path_name") val pathName: String? = null,
    @SerializedName("color_tag") val colorTag: String? = null,
    val order: Int = 0,
    val children: List<Category>? = null
)

// --- Theme ---

data class Theme(
    val id: Int,
    val name: String,
    val description: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    val images: List<ThemeImage>? = null
)

data class ThemeImage(
    val id: Int,
    val image: String,
    val label: String? = null
)

data class IPRequest(
    val name: String,
    val keywords: List<String>? = null,
    @SerializedName("subject_type") val subjectType: Int? = null,
    val order: Int? = null
)

data class CharacterRequest(
    val name: String,
    @SerializedName("ip_id") val ipId: Int,
    val avatar: String? = null,
    val gender: String = "other"
)

data class CategoryRequest(
    val name: String,
    val parent: Int? = null,
    @SerializedName("color_tag") val colorTag: String? = null,
    val order: Int? = null
)

data class MetadataOrderItem(
    val id: Int,
    val order: Int
)

data class IPBatchUpdateOrderRequest(
    val items: List<MetadataOrderItem>
)

data class IPBatchUpdateOrderResponse(
    val detail: String? = null,
    @SerializedName("updated_count") val updatedCount: Int = 0,
    val ips: List<IP> = emptyList()
)

data class CategoryBatchUpdateOrderRequest(
    val items: List<MetadataOrderItem>
)

data class CategoryBatchUpdateOrderResponse(
    val detail: String? = null,
    @SerializedName("updated_count") val updatedCount: Int = 0,
    val categories: List<Category> = emptyList()
)

data class ThemeRequest(
    val name: String,
    val description: String? = null
)

// --- Goods ---

data class UserRef(
    val id: Int,
    val username: String
)

data class GoodsListItem(
    val id: String,
    val name: String,
    val ip: IP,
    val characters: List<Character>,
    val category: Category,
    val theme: Theme? = null,
    @SerializedName("location_path") val locationPath: String? = null,
    @SerializedName("main_photo") val mainPhoto: String? = null,
    val status: String,
    val quantity: Int,
    @SerializedName("is_official") val isOfficial: Boolean = true,
    val user: UserRef? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val order: Long? = null
)

data class GoodsDetail(
    val id: String,
    val name: String,
    val ip: IP,
    val characters: List<Character>,
    val category: Category,
    val theme: Theme? = null,
    @SerializedName("location_path") val locationPath: String? = null,
    @SerializedName("main_photo") val mainPhoto: String? = null,
    val status: String,
    val quantity: Int,
    @SerializedName("is_official") val isOfficial: Boolean = true,
    val user: UserRef? = null,
    @SerializedName("user_id") val userId: Int? = null,
    val location: Int? = null,
    val price: String? = null,
    @SerializedName("purchase_date") val purchaseDate: String? = null,
    val notes: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("additional_photos") val additionalPhotos: List<GuziImage> = emptyList(),
    val order: Long? = null
)

data class GuziImage(
    val id: Int,
    val image: String,
    val label: String? = null
)

data class GoodsCreateRequest(
    val name: String,
    @SerializedName("ip_id") val ipId: Int,
    @SerializedName("character_ids") val characterIds: List<Int>,
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("theme_id") val themeId: Int? = null,
    val location: Int? = null,
    val status: String = "draft",
    val quantity: Int = 1,
    val price: String? = null,
    @SerializedName("purchase_date") val purchaseDate: String? = null,
    @SerializedName("is_official") val isOfficial: Boolean = true,
    val notes: String? = null,
    @SerializedName("merge_strategy") val mergeStrategy: String = "auto",
    @SerializedName("merge_target_id") val mergeTargetId: String? = null
)

data class GoodsDuplicateCandidate(
    val id: String,
    val name: String,
    val ip: IP,
    val characters: List<Character> = emptyList(),
    @SerializedName("main_photo_url") val mainPhotoUrl: String? = null,
    val quantity: Int = 1,
    @SerializedName("created_at") val createdAt: String? = null
)

data class GoodsDuplicateConflictResponse(
    val detail: String? = null,
    val code: String? = null,
    val candidates: List<GoodsDuplicateCandidate>? = null
)

data class GoodsMoveRequest(
    @SerializedName("anchor_id") val anchorId: String,
    val position: String // "before" | "after"
)

data class GoodsMoveResponse(
    val detail: String? = null,
    val id: String? = null,
    @SerializedName("new_order") val newOrder: Long? = null
)

// --- Showcase ---

data class Showcase(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerializedName("cover_image") val coverImage: String? = null,
    val order: Long = 0,
    @SerializedName("is_public") val isPublic: Boolean = true,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = "",
    @SerializedName("goods_count") val goodsCount: Int? = null,
    @SerializedName("preview_photos") val previewPhotos: List<String>? = null
)

data class ShowcaseDetail(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerializedName("cover_image") val coverImage: String? = null,
    val order: Long = 0,
    @SerializedName("is_public") val isPublic: Boolean = true,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = "",
    @SerializedName("showcase_goods") val showcaseGoods: List<ShowcaseGoods> = emptyList()
)

data class ShowcaseRequest(
    val name: String,
    val description: String? = null,
    @SerializedName("is_public") val isPublic: Boolean = true
)

data class ShowcaseGoods(
    val id: String,
    val goods: GoodsListItem,
    val order: Long = 0,
    val notes: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class ShowcaseAddGoodsRequest(
    @SerializedName("goods_id") val goodsId: String,
    val notes: String? = null
)

data class ShowcaseRemoveGoodsRequest(
    @SerializedName("goods_id") val goodsId: String
)

data class ShowcaseMoveGoodsRequest(
    @SerializedName("goods_id") val goodsId: String,
    @SerializedName("anchor_goods_id") val anchorGoodsId: String,
    val position: String
)

data class ShowcaseMoveGoodsResponse(
    val detail: String? = null,
    val id: String? = null,
    @SerializedName("new_order") val newOrder: Long? = null
)

// --- Location ---

data class StorageNode(
    val id: Int,
    val name: String,
    val parent: Int? = null,
    @SerializedName("path_name") val pathName: String? = null,
    val image: String? = null,
    val description: String? = null,
    val order: Int = 0
)

data class StorageNodeRequest(
    val name: String,
    val parent: Int? = null,
    val description: String? = null,
    val order: Int? = null
)

// --- Stats ---

data class GoodsStatsResponse(
    val meta: GoodsStatsMeta? = null,
    val overview: GoodsStatsOverview = GoodsStatsOverview(),
    val distributions: GoodsStatsDistributions? = null,
    val trends: GoodsStatsTrends? = null
)

data class GoodsStatsMeta(
    val top: Int = 10,
    @SerializedName("group_by") val groupBy: String = "month",
    @SerializedName("purchase_start") val purchaseStart: String? = null,
    @SerializedName("purchase_end") val purchaseEnd: String? = null,
    @SerializedName("created_start") val createdStart: String? = null,
    @SerializedName("created_end") val createdEnd: String? = null
)

data class GoodsStatsOverview(
    @SerializedName("goods_count") val goodsCount: Int = 0,
    @SerializedName("quantity_sum") val quantitySum: Int = 0,
    @SerializedName("value_sum") val valueSum: String = "0",
    @SerializedName("with_price_count") val withPriceCount: Int = 0,
    @SerializedName("missing_price_count") val missingPriceCount: Int = 0,
    @SerializedName("with_purchase_date_count") val withPurchaseDateCount: Int = 0,
    @SerializedName("missing_purchase_date_count") val missingPurchaseDateCount: Int = 0,
    @SerializedName("with_location_count") val withLocationCount: Int = 0,
    @SerializedName("missing_location_count") val missingLocationCount: Int = 0,
    @SerializedName("with_main_photo_count") val withMainPhotoCount: Int = 0,
    @SerializedName("missing_main_photo_count") val missingMainPhotoCount: Int = 0
)

data class GoodsStatsDistributions(
    val status: List<GoodsStatusDistributionItem>? = null,
    @SerializedName("is_official") val isOfficial: List<GoodsOfficialDistributionItem>? = null,
    @SerializedName("ip_subject_type") val ipSubjectType: List<GoodsSubjectTypeDistributionItem>? = null,
    @SerializedName("category_top") val categoryTop: List<GoodsCategoryTopItem>? = null,
    @SerializedName("ip_top") val ipTop: List<GoodsIPTopItem>? = null,
    @SerializedName("character_top") val characterTop: List<GoodsCharacterTopItem>? = null,
    @SerializedName("location_top") val locationTop: List<GoodsLocationTopItem>? = null
)

data class GoodsStatusDistributionItem(
    val status: String,
    val label: String,
    @SerializedName("goods_count") val goodsCount: Int = 0,
    @SerializedName("quantity_sum") val quantitySum: Int = 0
)

data class GoodsOfficialDistributionItem(
    @SerializedName("is_official") val isOfficial: Boolean,
    val label: String,
    @SerializedName("goods_count") val goodsCount: Int = 0,
    @SerializedName("quantity_sum") val quantitySum: Int = 0
)

data class GoodsSubjectTypeDistributionItem(
    @SerializedName("ip__subject_type") val subjectType: Int? = null,
    val label: String = "未知",
    @SerializedName("goods_count") val goodsCount: Int = 0,
    @SerializedName("quantity_sum") val quantitySum: Int = 0
)

data class GoodsCategoryTopItem(
    @SerializedName("category_id") val categoryId: Int,
    @SerializedName("category__name") val categoryName: String,
    @SerializedName("category__path_name") val categoryPathName: String? = null,
    @SerializedName("category__color_tag") val categoryColorTag: String? = null,
    @SerializedName("goods_count") val goodsCount: Int = 0,
    @SerializedName("quantity_sum") val quantitySum: Int = 0,
    @SerializedName("value_sum") val valueSum: String? = null
)

data class GoodsIPTopItem(
    @SerializedName("ip_id") val ipId: Int,
    @SerializedName("ip__name") val ipName: String,
    @SerializedName("ip__subject_type") val subjectType: Int? = null,
    @SerializedName("subject_type_label") val subjectTypeLabel: String? = null,
    @SerializedName("goods_count") val goodsCount: Int = 0,
    @SerializedName("quantity_sum") val quantitySum: Int = 0,
    @SerializedName("value_sum") val valueSum: String? = null
)

data class GoodsCharacterTopItem(
    @SerializedName("characters__id") val characterId: Int? = null,
    @SerializedName("characters__name") val characterName: String? = null,
    @SerializedName("characters__ip__id") val ipId: Int? = null,
    @SerializedName("characters__ip__name") val ipName: String? = null,
    @SerializedName("goods_count") val goodsCount: Int = 0,
    @SerializedName("quantity_sum") val quantitySum: Int = 0,
    @SerializedName("value_sum") val valueSum: String? = null
)

data class GoodsLocationTopItem(
    @SerializedName("location_id") val locationId: Int? = null,
    @SerializedName("location__name") val locationName: String? = null,
    @SerializedName("location__path_name") val locationPathName: String? = null,
    @SerializedName("goods_count") val goodsCount: Int = 0,
    @SerializedName("quantity_sum") val quantitySum: Int = 0,
    @SerializedName("value_sum") val valueSum: String? = null
)

data class GoodsTrendBucket(
    val bucket: String? = null,
    @SerializedName("goods_count") val goodsCount: Int = 0,
    @SerializedName("quantity_sum") val quantitySum: Int = 0,
    @SerializedName("value_sum") val valueSum: String? = null
)

data class GoodsStatsTrends(
    @SerializedName("purchase_date") val purchaseDate: List<GoodsTrendBucket>? = null,
    @SerializedName("created_at") val createdAt: List<GoodsTrendBucket>? = null
)

// --- Pagination ---

data class PaginatedResponse<T>(
    val count: Int = 0,
    val page: Int = 1,
    @SerializedName("page_size") val pageSize: Int = 0,
    val next: Int? = null,
    val previous: Int? = null,
    val results: List<T> = emptyList()
)

data class StandardPaginatedResponse<T>(
    val count: Int = 0,
    val next: String? = null,
    val previous: String? = null,
    val results: List<T> = emptyList()
)

// --- Bangumi Import ---

data class BgmSearchSubjectsRequest(
    val keyword: String,
    @SerializedName("subject_type") val subjectType: Int? = null
)

data class BgmSearchCharactersRequest(
    @SerializedName("ip_name") val ipName: String,
    @SerializedName("subject_type") val subjectType: Int? = null
)

data class BgmSubject(
    val id: Int,
    val name: String,
    @SerializedName("name_cn") val nameCn: String? = null,
    val type: Int? = null,
    @SerializedName("type_name") val typeName: String? = null,
    val image: String? = null
)

data class BgmSearchSubjectsResponse(
    val subjects: List<BgmSubject> = emptyList()
)

data class BgmGetCharactersRequest(
    @SerializedName("subject_id") val subjectId: Int
)

data class BgmCharacter(
    val name: String,
    val relation: String? = null,
    val avatar: String? = null
)

data class BgmSearchCharactersResponse(
    @SerializedName("ip_name") val ipName: String,
    val characters: List<BgmCharacter> = emptyList()
)

data class BgmGetCharactersResponse(
    @SerializedName("subject_id") val subjectId: Int,
    @SerializedName("subject_name") val subjectName: String,
    val characters: List<BgmCharacter> = emptyList()
)

data class BgmCreateCharacterItem(
    @SerializedName("ip_name") val ipName: String,
    @SerializedName("character_name") val characterName: String,
    @SerializedName("subject_type") val subjectType: Int? = null,
    val avatar: String? = null
)

data class BgmCreateCharactersRequest(
    val characters: List<BgmCreateCharacterItem>
)

data class BgmCreateCharacterResult(
    @SerializedName("ip_name") val ipName: String,
    @SerializedName("character_name") val characterName: String,
    val status: String,
    @SerializedName("ip_id") val ipId: Int? = null,
    @SerializedName("character_id") val characterId: Int? = null,
    val error: String? = null
)

data class BgmCreateCharactersResponse(
    val created: Int = 0,
    val skipped: Int = 0,
    val details: List<BgmCreateCharacterResult> = emptyList()
)
