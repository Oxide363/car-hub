package com.carhub.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

/**
 * Walks ONLY the owner-granted CARHUB tree (via Storage Access Framework) and
 * classifies media by extension. Never touches storage outside the granted tree.
 */
object MediaIndexer {

    private val VIDEO = setOf("mp4", "mkv", "webm", "3gp", "m4v", "mov", "ts", "avi")
    private val AUDIO = setOf("mp3", "aac", "m4a", "wav", "ogg", "flac", "opus")

    fun index(context: Context, treeUri: Uri): List<MediaEntry> {
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        val out = ArrayList<MediaEntry>()
        walk(context, root, "", out)
        return out.sortedWith(compareBy({ it.folder }, { it.title.lowercase() }))
    }

    private fun walk(context: Context, dir: DocumentFile, path: String, out: MutableList<MediaEntry>) {
        val children = try { dir.listFiles() } catch (e: Exception) { return }
        for (f in children) {
            val name = f.name ?: continue
            if (f.isDirectory) {
                val next = if (path.isEmpty()) name else "$path/$name"
                walk(context, f, next, out)
            } else {
                val ext = name.substringAfterLast('.', "").lowercase()
                val type = when {
                    ext in VIDEO -> MediaType.VIDEO
                    ext in AUDIO -> MediaType.AUDIO
                    else -> null
                } ?: continue
                out.add(
                    MediaEntry(
                        uri = f.uri.toString(),
                        name = name,
                        type = type,
                        folder = path,
                        durationMs = readDuration(context, f.uri)
                    )
                )
            }
        }
    }

    private fun readDuration(context: Context, uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            try { retriever.release() } catch (e: Exception) { }
        }
    }
}
