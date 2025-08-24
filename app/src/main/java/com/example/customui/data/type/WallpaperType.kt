package com.example.customui.data.type

import kotlinx.serialization.Serializable

@Serializable
data class WallpaperData(
    val current_page: Int,
    val total_items: Int,
    val current_items: Int,
    val total_pages: Int,
    val data: List<WallpaperItems>
)

@Serializable
data class WallpaperItems(
    val id: String,
    val name: String,
    val WallpaperImage: List<WallpaperImage>
)

@Serializable
data class WallpaperImage(
    val id: String,
    val image: String
)

//detail
@Serializable
data class WallpaperDetailResponse(
    val data: List<WallpaperDetailType>
)

@Serializable
data class WallpaperDetailType(
    val id: String,
    val name:String,
    val WallpaperDetail: WallpaperDetailItems,
    val WallpaperImage: List<WallpaperImage>
)

@Serializable
data class WallpaperDetailItems(
    val artist:String,
    val createdAt:String,
    val minSolution:String,
    val maxSolution:String,
)

