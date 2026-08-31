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
import com.carhub.data.IndexResult
import com.carhub.data.MapRegion
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

enum class Section { HOME, MOVIES, MUSIC, GAMES, KIDS, CONTENT, SETTINGS }

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = Prefs(app)

    var loaded by mutableStateOf(false); private set
    var hasPin by mutableStateOf(false); private set
    var hasPattern by mutableStateOf(false); private set
    var lockoutUntil by mutableStateOf(0L); private set
    var mode by mutableStateOf(Mode.OWNER); private set
    var tier by mutableStateOf("B"); private set
    var treeUri by mutableStateOf<String?>(null); private set
    var media by mutableStateOf<List<MediaEntry>>(emptyList()); private set
    var mapRegions by mutableStateOf<List<MapRegion>>(emptyList()); private set
    var indexing by mutableStateOf(false); private set

    var section by mutableStateOf(Section.HOME)
    var playing by mutableStateOf<MediaEntry?>(null)     // full-screen video overlay
    var playStartMs by mutableStateOf(0L); private set
    var askExitPin by mutableStateOf(false)

    var favorites by mutableStateOf<Set<String>>(emptySet()); private set
    var kidsCategories by mutableStateOf<Set<String>>(emptySet()); private set
    var brightness by mutableStateOf(-1f); private set   // -1 = system; else 0.05..1.0
    var continueList by mutableStateOf<List<MediaEntry>>(emptyList()); private set

    // Shared audio player — survives section changes so music keeps playing.
    private var audio: ExoPlayer? = null
    var nowPlaying by mutableStateOf<String?>(null); private set
    var audioIsPlaying by mutableStateOf(false); private set

    private var resume: MutableMap<String, LongArray> = mutableMapOf() // uri -> [pos, dur, at]

    init {
        viewModelScope.launch {
            hasPin = prefs.pinHash.first() != null
            hasPattern = prefs.patternHash.first() != null
            treeUri = prefs.treeUri.first()
            mode = if (prefs.passenger.first()) Mode.PASSENGER else Mode.OWNER
            tier = Kiosk.tier(getApplication())
            favorites = prefs.favorites.first()
            kidsCategories = prefs.kidsCategories.first()
            brightness = prefs.brightness.first()
            loadResume(prefs.resumeJson.first())
            loaded = true
            treeUri?.let { reindex(it) }
        }
    }

    // ----- credential hashing: salted PBKDF2-HMAC-SHA256 -----
    private fun pbkdf2(secret: String, salt: ByteArray): ByteArray {
        val spec = javax.crypto.spec.PBEKeySpec(secret.toCharArray(), salt, 120_000, 256)
        return javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec).encoded
    }

    private fun hashSecret(secret: String): String {
        val salt = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        val h = pbkdf2(secret, salt)
        return android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP) + ":" +
            android.util.Base64.encodeToString(h, android.util.Base64.NO_WRAP)
    }

    private fun verifySecret(secret: String, stored: String?): Boolean {
        if (stored == null) return false
        val parts = stored.split(":")
        if (parts.size != 2) return false
        return try {
            val salt = android.util.Base64.decode(parts[0], android.util.Base64.NO_WRAP)
            val expected = android.util.Base64.decode(parts[1], android.util.Base64.NO_WRAP)
            MessageDigest.isEqual(pbkdf2(secret, salt), expected) // constant-time compare
        } catch (e: Exception) { false }
    }

    // ----- brute-force lockout (escalating) -----
    private var failCount = 0
    private var strikeouts = 0

    fun lockedOut(): Boolean = System.currentTimeMillis() < lockoutUntil
    fun lockRemainingSec(): Long =
        ((lockoutUntil - System.currentTimeMillis()).coerceAtLeast(0L) + 999L) / 1000L

    private fun registerAttempt(ok: Boolean) {
        if (ok) { failCount = 0; strikeouts = 0; lockoutUntil = 0L } else {
            failCount++
            if (failCount >= 5) {
                strikeouts++
                val secs = 30L * (1 shl (strikeouts - 1).coerceAtMost(4))
                lockoutUntil = System.currentTimeMillis() + secs * 1000L
                failCount = 0
            }
        }
    }

    fun setupPin(pin: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val h = hashSecret(pin)
            prefs.setPin(h)
            hasPin = true
        }
    }

    fun changePin(pin: String) = setupPin(pin)

    suspend fun verifyPin(pin: String): Boolean {
        if (lockedOut()) return false
        val stored = prefs.pinHash.first()
        val ok = withContext(Dispatchers.Default) { verifySecret(pin, stored) }
        registerAttempt(ok)
        return ok
    }

    fun setupPattern(seq: List<Int>) {
        viewModelScope.launch(Dispatchers.Default) {
            val h = hashSecret(seq.joinToString("-"))
            prefs.setPattern(h)
            hasPattern = true
        }
    }

    suspend fun verifyPattern(seq: List<Int>): Boolean {
        if (lockedOut()) return false
        val stored = prefs.patternHash.first()
        val ok = withContext(Dispatchers.Default) { verifySecret(seq.joinToString("-"), stored) }
        registerAttempt(ok)
        return ok
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
            val result = withContext(Dispatchers.IO) {
                try { MediaIndexer.index(getApplication(), Uri.parse(uriStr)) }
                catch (e: Exception) { IndexResult(emptyList(), emptyList()) }
            }
            media = result.media
            mapRegions = result.maps
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

    // ----- screen brightness (per-app window; no system permission needed) -----
    fun setBrightness(v: Float) {
        brightness = v
        viewModelScope.launch { prefs.setBrightness(v) }
    }

    /** Cycles System → Dim → Night → System. */
    fun cycleBrightness() {
        val next = when {
            brightness < 0f -> 0.35f
            brightness > 0.2f -> 0.10f
            else -> -1f
        }
        setBrightness(next)
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
