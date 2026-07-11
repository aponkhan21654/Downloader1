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

// --- Apify Models ---

@JsonClass(generateAdapter = true)
data class ApifyProxySettings(
    @Json(name = "useApifyProxy") val useApifyProxy: Boolean = true,
    @Json(name = "apifyProxyGroups") val apifyProxyGroups: List<String> = listOf("RESIDENTIAL"),
    @Json(name = "apifyProxyCountry") val apifyProxyCountry: String = "US"
)

@JsonClass(generateAdapter = true)
data class ApifyMergeYoutube(
    @Json(name = "quality") val quality: Int = 720
)

@JsonClass(generateAdapter = true)
data class ApifyRequest(
    @Json(name = "url") val url: String,
    @Json(name = "mergeAV") val mergeAV: Boolean? = null,
    @Json(name = "mergeYoutube") val mergeYoutube: ApifyMergeYoutube? = null,
    @Json(name = "proxySettings") val proxySettings: ApifyProxySettings = ApifyProxySettings()
)

@JsonClass(generateAdapter = true)
data class ApifyResponseItem(
    @Json(name = "title") val title: String? = null,
    @Json(name = "uploader") val uploader: String? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "formats") val formats: List<ApifyFormatItem>? = null,
    @Json(name = "download") val download: List<ApifyDownloadItem>? = null
)

@JsonClass(generateAdapter = true)
data class ApifyFormatItem(
    @Json(name = "resolution") val resolution: String? = null,
    @Json(name = "url") val url: String? = null
)

@JsonClass(generateAdapter = true)
data class ApifyDownloadItem(
    @Json(name = "url") val url: String? = null
)
