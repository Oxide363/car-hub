package com.carhub.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream

/**
 * Real artwork for the library: album art embedded in audio files, and a poster
 * frame extracted from video. Results are cached in memory and on disk so the
 * grid stays fast and extraction happens at most once per file.
 */
object Thumbs {

    private const val MAX = 512
    private val mem = LruCache<String, ImageBitmap>(120)

    /** Non-blocking peek for already-decoded art (safe to call during composition). */
    fun cached(key: String): ImageBitmap? = mem.get(key)

    /** Blocking load — call off the main thread. Returns null if the file has no art. */
    fun load(context: Context, entry: MediaEntry): ImageBitmap? {
        mem.get(entry.uri)?.let { return it }
        val file = File(context.cacheDir, "thumb_${(entry.uri.hashCode() and 0x7FFFFFFF)}.jpg")
        val bmp: Bitmap? = when {
            file.exists() -> BitmapFactory.decodeFile(file.absolutePath)
            else -> extract(context, entry)?.also { save(it, file) }
        }
        val img = bmp?.asImageBitmap()
        if (img != null) mem.put(entry.uri, img)
        return img
    }

    private fun save(bmp: Bitmap, file: File) {
        try {
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        } catch (e: Exception) {
            // Non-fatal: we just won't have a disk cache for this item.
        }
    }

    private fun extract(context: Context, entry: MediaEntry): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(entry.uri))
            val raw = if (entry.type == MediaType.AUDIO) {
                retriever.embeddedPicture?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            } else {
                retriever.getFrameAtTime(5_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?: retriever.frameAtTime
            }
            raw?.let { scale(it) }
        } catch (e: Exception) {
            null
        } finally {
            try { retriever.release() } catch (e: Exception) { }
        }
    }

    private fun scale(b: Bitmap): Bitmap {
        val w = b.width
        val h = b.height
        if (w <= 0 || h <= 0) return b
        if (w <= MAX && h <= MAX) return b
        val r = minOf(MAX.toFloat() / w, MAX.toFloat() / h)
        return Bitmap.createScaledBitmap(
            b, (w * r).toInt().coerceAtLeast(1), (h * r).toInt().coerceAtLeast(1), true
        )
    }
}
