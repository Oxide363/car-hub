package com.carhub

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.carhub.data.MediaEntry
import com.carhub.data.MediaIndexer
import com.carhub.data.MediaType
import com.carhub.data.Prefs
import com.carhub.security.Kiosk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

enum class Mode { OWNER, PASSENGER }

enum class Section { HOME, MOVIES, MUSIC, GAMES, MAPS, KIDS, CONTENT, SETTINGS }

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)

    var loaded by mutableStateOf(false); private set
    var hasPin by mutableStateOf(false); private set
    var mode by mutableStateOf(Mode.OWNER); private set
    var tier by mutableStateOf("B"); private set
    var treeUri by mutableStateOf<String?>(null); private set
    var media by mutableStateOf<List<MediaEntry>>(emptyList()); private set
    var indexing by mutableStateOf(false); private set

    var section by mutableStateOf(Section.HOME)
    var playing by mutableStateOf<MediaEntry?>(null)     // full-screen video overlay
    var playStartMs by mutableStateOf(0L); private set
    var askExitPin by mutableStateOf(false)

    var favorites by mutableStateOf<Set<String>>(emptySet()); private set
    var kidsCategories by mutableStateOf<Set<String>>(emptySet()); private set
    var continueList by mutableStateOf<List<MediaEntry>>(emptyList()); private set

    // Shared audio player — survives section changes so music keeps playing.
    private var audio: ExoPlayer? = null
    var nowPlaying by mutableStateOf<String?>(null); private set
    var audioIsPlaying by mutableStateOf(false); private set

    private var resume: MutableMap<String, LongArray> = mutableMapOf() // uri -> [pos, dur, at]

    init {
        viewModelScope.launch {
            hasPin = prefs.pinHash.first() != null
            treeUri = prefs.treeUri.first()
            mode = if (prefs.passenger.first()) Mode.PASSENGER else Mode.OWNER
            tier = Kiosk.tier(getApplication())
            favorites = prefs.favorites.first()
            kidsCategories = prefs.kidsCategories.first()
            loadResume(prefs.resumeJson.first())
            loaded = true
            treeUri?.let { reindex(it) }
        }
    }

    private fun sha(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray())
            .joinToString("") { "%02x".format(it.toInt() and 0xFF) }

    fun setupPin(pin: String) {
        viewModelScope.launch { prefs.setPin(sha(pin)); hasPin = true }
    }

    fun changePin(pin: String) = setupPin(pin)

    suspend fun verifyPin(pin: String): Boolean {
        val stored = prefs.pinHash.first() ?: return false
        return stored == sha(pin)
    }

    fun setTree(uri: Uri) {
        viewModelScope.launch {
            prefs.setTree(uri.toString())
            treeUri = uri.toString()
            reindex(uri.toString())
        }
    }

    private fun reindex(uriStr: String) {
        viewModelScope.launch {
            indexing = true
            media = withContext(Dispatchers.IO) {
                try { MediaIndexer.index(getApplication(), Uri.parse(uriStr)) }
                catch (e: Exception) { emptyList() }
            }
            indexing = false
            refreshContinue()
        }
    }

    fun rescan() { treeUri?.let { reindex(it) } }

    val movies: List<MediaEntry> get() = media.filter { it.type == MediaType.VIDEO }
    val songs: List<MediaEntry> get() = media.filter { it.type == MediaType.AUDIO }

    fun kidsMovies(): List<MediaEntry> =
        if (kidsCategories.isEmpty()) emptyList() else movies.filter { it.category in kidsCategories }

    fun kidsSongs(): List<MediaEntry> =
        if (kidsCategories.isEmpty()) emptyList() else songs.filter { it.category in kidsCategories }

    val allCategories: List<String>
        get() = media.map { it.category }.distinct().sorted()

    fun go(s: Section) { section = s }

    // ----- video -----
    fun openMovie(e: MediaEntry) {
        playStartMs = resume[e.uri]?.get(0) ?: 0L
        playing = e
    }

    fun recordResume(entry: MediaEntry, pos: Long, dur: Long) {
        if (pos <= 0) return
        resume[entry.uri] = longArrayOf(pos, dur, System.currentTimeMillis())
        persistResume()
        refreshContinue()
    }

    private fun loadResume(json: String) {
        resume = mutableMapOf()
        try {
            val obj = org.json.JSONObject(json)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val o = obj.getJSONObject(k)
                resume[k] = longArrayOf(o.optLong("pos"), o.optLong("dur"), o.optLong("at"))
            }
        } catch (e: Exception) { /* corrupt state: start fresh */ }
    }

    private fun persistResume() {
        val obj = org.json.JSONObject()
        for ((k, v) in resume) {
            obj.put(k, org.json.JSONObject().put("pos", v[0]).put("dur", v[1]).put("at", v[2]))
        }
        val s = obj.toString()
        viewModelScope.launch { prefs.setResumeJson(s) }
    }

    private fun refreshContinue() {
        val byUri = media.associateBy { it.uri }
        continueList = resume.entries
            .filter { it.value[0] > 3000 && (it.value[1] <= 0 || it.value[0] < it.value[1] - 5000) }
            .sortedByDescending { it.value[2] }
            .mapNotNull { byUri[it.key] }
            .take(12)
    }

    // ----- audio -----
    private fun ensureAudio(): ExoPlayer {
        audio?.let { return it }
        val p = ExoPlayer.Builder(getApplication<Application>()).build()
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) { audioIsPlaying = isPlaying }
        })
        audio = p
        return p
    }

    fun playAudio(e: MediaEntry) {
        val p = ensureAudio()
        p.setMediaItem(MediaItem.fromUri(Uri.parse(e.uri)))
        p.prepare()
        p.play()
        nowPlaying = e.title
    }

    fun toggleAudio() {
        val p = audio ?: return
        if (p.isPlaying) p.pause() else p.play()
    }

    override fun onCleared() {
        audio?.release()
        audio = null
    }

    // ----- favorites / kids -----
    fun toggleFavorite(uri: String) {
        favorites = if (uri in favorites) favorites - uri else favorites + uri
        viewModelScope.launch { prefs.setFavorites(favorites) }
    }

    fun isFavorite(uri: String) = uri in favorites

    fun toggleKidsCategory(cat: String) {
        kidsCategories = if (cat in kidsCategories) kidsCategories - cat else kidsCategories + cat
        viewModelScope.launch { prefs.setKidsCategories(kidsCategories) }
    }

    // ----- mode -----
    fun startPassenger() {
        viewModelScope.launch { prefs.setPassenger(true) }
        mode = Mode.PASSENGER
        section = Section.HOME
    }

    fun exitPassenger() {
        viewModelScope.launch { prefs.setPassenger(false) }
        mode = Mode.OWNER
        askExitPin = false
        section = Section.HOME
    }
}
