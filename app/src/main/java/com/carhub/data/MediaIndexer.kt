package com.carhub.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Walks ONLY the owner-granted CARHUB tree (via Storage Access Framework) and
 * classifies media by extension. Never touches storage outside the granted tree.
 *
 * Handles: sidecar subtitles (.srt/.vtt/.ass next to a video) and multi-part
 * movies (Movie.part1.mkv / Movie.CD2.mkv ...) grouped into one playable entry.
 */
object MediaIndexer {

    private val VIDEO = setOf("mp4", "mkv", "webm", "3gp", "m4v", "mov", "ts", "avi")
    private val AUDIO = setOf("mp3", "aac", "m4a", "wav", "ogg", "flac", "opus")
    private val SUBS = setOf("srt", "vtt", "ass", "ssa", "sub")

    private val PART = Regex("(?i)^(.*?)[ _.\\-]*(?:part|pt|cd|disc)[ _.\\-]*(\\d{1,2})$")
    private val NUM = Regex("^(.*?)[ _.\\-]+(\\d{1,2})$")

    private class Raw(val uri: String, val name: String, val folder: String) {
        val noExt: String get() = name.substringBeforeLast('.', name)
    }

    fun index(context: Context, treeUri: Uri): IndexResult {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return IndexResult(emptyList(), emptyList())
        val videos = ArrayList<Raw>()
        val audios = ArrayList<Raw>()
        val subs = HashMap<String, Pair<String, String>>() // "folder|basename" -> (uri, ext)
        val maps = ArrayList<MapRegion>()
        walk(root, "", videos, audios, subs, maps)

        val out = ArrayList<MediaEntry>()

        for (a in audios) {
            out.add(
                MediaEntry(
                    uri = a.uri, name = a.name, type = MediaType.AUDIO,
                    folder = a.folder, durationMs = readDuration(context, a.uri)
                )
            )
        }

        groupVideos(context, videos, subs, out)

        return IndexResult(
            out.sortedWith(compareBy({ it.folder }, { it.title.lowercase() })),
            maps.sortedBy { it.name.lowercase() }
        )
    }

    private fun walk(
        dir: DocumentFile, path: String,
        videos: MutableList<Raw>, audios: MutableList<Raw>,
        subs: MutableMap<String, Pair<String, String>>, maps: MutableList<MapRegion>
    ) {
        val children = try { dir.listFiles() } catch (e: Exception) { return }
        for (f in children) {
            val name = f.name ?: continue
            if (f.isDirectory) {
                val next = if (path.isEmpty()) name else "$path/$name"
                walk(f, next, videos, audios, subs, maps)
            } else {
                val ext = name.substringAfterLast('.', "").lowercase()
                when {
                    ext in VIDEO -> videos.add(Raw(f.uri.toString(), name, path))
                    ext in AUDIO -> audios.add(Raw(f.uri.toString(), name, path))
                    ext in SUBS -> {
                        val base = name.substringBeforeLast('.', name).lowercase()
                        subs["$path|$base"] = f.uri.toString() to ext
                    }
                    ext == "map" -> maps.add(MapRegion(name.substringBeforeLast('.', name), f.uri.toString()))
                }
            }
        }
    }

    private fun groupVideos(
        context: Context, videos: List<Raw>,
        subs: Map<String, Pair<String, String>>, out: MutableList<MediaEntry>
    ) {
        // Bucket candidate parts by folder + stripped base; keep the rest standalone.
        val groups = HashMap<String, MutableList<Pair<Int, Raw>>>()
        val standalone = ArrayList<Raw>()
        for (v in videos) {
            val split = splitPart(v.noExt)
            if (split != null) {
                groups.getOrPut("${v.folder}|${split.first.lowercase()}") { ArrayList() }
                    .add(split.second to v)
            } else standalone.add(v)
        }

        for ((key, parts) in groups) {
            if (parts.size < 2) {
                // A lone "…1" file is not really multi-part.
                standalone.add(parts[0].second)
                continue
            }
            val ordered = parts.sortedBy { it.first }.map { it.second }
            val first = ordered.first()
            val base = key.substringAfterLast('|')
            val displayName = first.noExt.let { n -> splitPart(n)?.first ?: n }
            val total = ordered.sumOf { readDuration(context, it.uri) }
            val sub = findSub(first.folder, listOf(base, first.noExt.lowercase()), subs)
            out.add(
                MediaEntry(
                    uri = first.uri, name = displayName, type = MediaType.VIDEO,
                    folder = first.folder, durationMs = total,
                    subtitleUri = sub?.first, subtitleExt = sub?.second,
                    partUris = ordered.map { it.uri }
                )
            )
        }

        for (v in standalone) {
            val sub = findSub(v.folder, listOf(v.noExt.lowercase()), subs)
            out.add(
                MediaEntry(
                    uri = v.uri, name = v.name, type = MediaType.VIDEO,
                    folder = v.folder, durationMs = readDuration(context, v.uri),
                    subtitleUri = sub?.first, subtitleExt = sub?.second
                )
            )
        }
    }

    private fun findSub(
        folder: String, baseNames: List<String>, subs: Map<String, Pair<String, String>>
    ): Pair<String, String>? {
        for (b in baseNames) subs["$folder|$b"]?.let { return it }
        return null
    }

    private fun splitPart(noExt: String): Pair<String, Int>? {
        PART.matchEntire(noExt)?.let {
            val base = it.groupValues[1].trim(' ', '_', '.', '-')
            if (base.isNotEmpty()) return base to (it.groupValues[2].toIntOrNull() ?: 0)
        }
        NUM.matchEntire(noExt)?.let {
            val base = it.groupValues[1].trim(' ', '_', '.', '-')
            if (base.length >= 2) return base to (it.groupValues[2].toIntOrNull() ?: 0)
        }
        return null
    }

    private fun readDuration(context: Context, uri: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(uri))
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            try { retriever.release() } catch (e: Exception) { }
        }
    }
}
