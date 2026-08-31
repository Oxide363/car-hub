package com.carhub.data

enum class MediaType { VIDEO, AUDIO }

/** An offline map region file (Mapsforge .map) found in the CARHUB folder. */
data class MapRegion(val name: String, val uri: String)

/** Result of one library scan: playable media plus any offline map regions. */
data class IndexResult(val media: List<MediaEntry>, val maps: List<MapRegion>)

data class MediaEntry(
    val uri: String,
    val name: String,
    val type: MediaType,
    val folder: String,      // relative path inside CARHUB, e.g. "Movies/Telugu"
    val durationMs: Long = 0L,
    val subtitleUri: String? = null,   // sidecar .srt/.vtt/.ass next to a video
    val subtitleExt: String? = null,
    val partUris: List<String> = emptyList()  // >1 => multi-part movie, played as a queue
) {
    /** URIs to play, in order (single file, or all parts of a multi-part movie). */
    val playUris: List<String> get() = if (partUris.isEmpty()) listOf(uri) else partUris

    val isMultiPart: Boolean get() = partUris.size > 1

    /** Display title without file extension. */
    val title: String get() = name.substringBeforeLast('.', name)

    /** Top-level category under the media root, e.g. "Telugu" for "Movies/Telugu". */
    val category: String
        get() {
            val parts = folder.split('/').filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> parts[1]
                parts.size == 1 -> parts[0]
                else -> "General"
            }
        }

    val durationLabel: String
        get() {
            if (durationMs <= 0L) return ""
            val totalMin = durationMs / 60000
            val h = totalMin / 60
            val m = totalMin % 60
            return if (h > 0) "${h}h ${m.toString().padStart(2, '0')}m" else "${m}m"
        }
}
