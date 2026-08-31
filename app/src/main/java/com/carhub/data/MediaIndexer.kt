package com.carhub.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Walks ONLY the owner-granted CARHUB tree (via Storage Access Framework) and
 * classifies media by extension. Never touches storage outside the granted tree.
 *
 * Grouping:
 *  - single video            -> one movie tile
 *  - a few parts of one film -> one movie tile, played back-to-back (partUris)
 *  - a TV series (episodes)  -> one collection tile -> episode list (episodes)
 *  - a music folder          -> one album tile -> track list (episodes)
 *  - sidecar subtitles are matched to their video.
 */
object MediaIndexer {

    private val VIDEO = setOf("mp4", "mkv", "webm", "3gp", "m4v", "mov", "ts", "avi")
    private val AUDIO = setOf("mp3", "aac", "m4a", "wav", "ogg", "flac", "opus")
    private val SUBS = setOf("srt", "vtt", "ass", "ssa", "sub")

    private val PART = Regex("(?i)^(.*?)[ _.\\-]*(?:part|pt|cd|disc)[ _.\\-]*(\\d{1,2})$")
    private val NUM = Regex("^(.*?)[ _.\\-]+(\\d{1,2})$")
    private val EPISODE = Regex(
        "(?i)(\\bs\\d{1,2}[ ._-]?e\\d{1,2}\\b|\\b\\d{1,2}x\\d{1,2}\\b|\\bepisode[ ._-]?\\d{1,2}\\b|\\bep[ ._-]?\\d{1,2}\\b)"
    )

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
        buildVideos(context, videos, subs, out)
        buildAudioAlbums(context, audios, out)

        return IndexResult(
            out.sortedWith(compareBy({ it.title.lowercase() })),
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

    // ----- videos: singles, multi-part movies, series collections -----

    private fun buildVideos(
        context: Context, videos: List<Raw>,
        subs: Map<String, Pair<String, String>>, out: MutableList<MediaEntry>
    ) {
        val groups = LinkedHashMap<String, MutableList<Triple<Int, Raw, Boolean>>>()
        val standalone = ArrayList<Raw>()
        for (v in videos) {
            val info = videoBase(v.noExt)
            if (info != null) {
                val marker = EPISODE.containsMatchIn(v.noExt)
                groups.getOrPut("${v.folder}|${info.first.lowercase()}") { ArrayList() }
                    .add(Triple(info.second, v, marker))
            } else standalone.add(v)
        }

        for ((_, members) in groups) {
            if (members.size < 2) { standalone.add(members[0].second); continue }
            val ordered = members.sortedBy { it.first }
            val first = ordered.first().second
            val displayName = videoBase(first.noExt)?.first ?: first.noExt
            val isSeries = ordered.any { it.third } || ordered.size > 3
            if (isSeries) {
                val eps = ordered.map { (_, raw, _) -> videoEntry(context, raw, subs) }
                out.add(
                    MediaEntry(
                        uri = first.uri, name = displayName, type = MediaType.VIDEO,
                        folder = first.folder, episodes = eps
                    )
                )
            } else {
                val total = ordered.sumOf { readDuration(context, it.second.uri) }
                val sub = findSub(first.folder, listOf(first.noExt.lowercase()), subs)
                out.add(
                    MediaEntry(
                        uri = first.uri, name = displayName, type = MediaType.VIDEO,
                        folder = first.folder, durationMs = total,
                        subtitleUri = sub?.first, subtitleExt = sub?.second,
                        partUris = ordered.map { it.second.uri }
                    )
                )
            }
        }

        for (v in standalone) out.add(videoEntry(context, v, subs))
    }

    private fun videoEntry(context: Context, v: Raw, subs: Map<String, Pair<String, String>>): MediaEntry {
        val sub = findSub(v.folder, listOf(v.noExt.lowercase()), subs)
        return MediaEntry(
            uri = v.uri, name = v.name, type = MediaType.VIDEO, folder = v.folder,
            durationMs = readDuration(context, v.uri),
            subtitleUri = sub?.first, subtitleExt = sub?.second
        )
    }

    private fun videoBase(noExt: String): Pair<String, Int>? {
        EPISODE.find(noExt)?.let { m ->
            val base = noExt.substring(0, m.range.first).trim(' ', '_', '.', '-')
            val num = Regex("\\d{1,2}").findAll(m.value).map { it.value.toInt() }.lastOrNull() ?: 0
            if (base.length >= 2) return base to num
        }
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

    // ----- audio: one album per folder -----

    private fun buildAudioAlbums(context: Context, audios: List<Raw>, out: MutableList<MediaEntry>) {
        val byFolder = LinkedHashMap<String, MutableList<Raw>>()
        for (a in audios) byFolder.getOrPut(a.folder) { ArrayList() }.add(a)
        for ((folder, songs) in byFolder) {
            val ordered = songs.sortedBy { it.name.lowercase() }
            val albumName = folder.substringAfterLast('/').ifBlank { "Music" }
            val tracks = ordered.map { s ->
                MediaEntry(
                    uri = s.uri, name = s.name, type = MediaType.AUDIO, folder = s.folder,
                    durationMs = readDuration(context, s.uri)
                )
            }
            out.add(
                MediaEntry(
                    uri = ordered.first().uri, name = albumName, type = MediaType.AUDIO,
                    folder = folder, episodes = tracks
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
