package com.carhub

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    var askExitPin by mutableStateOf(false)

    init {
        viewModelScope.launch {
            hasPin = prefs.pinHash.first() != null
            treeUri = prefs.treeUri.first()
            mode = if (prefs.passenger.first()) Mode.PASSENGER else Mode.OWNER
            tier = Kiosk.tier(getApplication())
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
        }
    }

    fun rescan() { treeUri?.let { reindex(it) } }

    val movies: List<MediaEntry> get() = media.filter { it.type == MediaType.VIDEO }
    val songs: List<MediaEntry> get() = media.filter { it.type == MediaType.AUDIO }

    fun go(s: Section) { section = s }

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
