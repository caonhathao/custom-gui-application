package com.example.customui.data.type

import kotlinx.serialization.Serializable

@Serializable
data class ActionCenterData(
    val current_page: Int,
    val total_items: Int,
    val current_items: Int,
    val total_pages: Int,
    val data: List<WallpaperItems>
)

@Serializable
data class ActionCenterItems(
    val id: String,
    val name: String,
    val ActionCenterImage: List<ActionCenterImage>
)

@Serializable
data class ActionCenterImage(
    val id: String,
    val image: String
)

//detail
@Serializable
data class ActionCenterDetailResponse(
    val data: List<ActionCenterDetailType>
)

@Serializable
data class ActionCenterDetailType(
    val id: String,
    val name: String,
    val ActionCenterDetail: ActionCenterDetailItems,
    val ActionCenterImage: List<WallpaperImage>
)

@Serializable
data class ActionCenterDetailItems(
    val artist: String,
    val createdAt: String
)

