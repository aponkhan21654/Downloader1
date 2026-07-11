package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CobaltRequest(
    @Json(name = "url") val url: String,
    @Json(name = "videoQuality") val videoQuality: String = "1080",
    @Json(name = "audioFormat") val audioFormat: String = "mp3",
    @Json(name = "audioOnly") val audioOnly: Boolean = false,
    @Json(name = "filenamePattern") val filenamePattern: String = "classic",
    @Json(name = "twitterGif") val twitterGif: Boolean = true,
    @Json(name = "youtubeVideoCodec") val youtubeVideoCodec: String = "h264"
)

@JsonClass(generateAdapter = true)
data class CobaltResponse(
    @Json(name = "status") val status: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "filename") val filename: String? = null,
    @Json(name = "picker") val picker: List<PickerItem>? = null,
    @Json(name = "error") val error: CobaltErrorDetail? = null
)

@JsonClass(generateAdapter = true)
data class PickerItem(
    @Json(name = "url") val url: String,
    @Json(name = "type") val type: String? = "video", // video, photo
    @Json(name = "thumb") val thumb: String? = null
)

@JsonClass(generateAdapter = true)
data class CobaltErrorDetail(
    @Json(name = "code") val code: String? = null,
    @Json(name = "text") val text: String? = null
)
