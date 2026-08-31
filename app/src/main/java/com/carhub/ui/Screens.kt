package com.carhub.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeUp
import android.media.AudioManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.common.C
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Slider
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.scale
import com.carhub.data.Thumbs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.carhub.MainViewModel
import com.carhub.Mode
import com.carhub.Section
import com.carhub.data.MediaEntry
import kotlinx.coroutines.launch

// ---------- shared styling helpers ----------

@Composable
private fun fieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = CH.TextPrimary,
    unfocusedTextColor = CH.TextPrimary,
    focusedBorderColor = CH.Accent,
    unfocusedBorderColor = CH.Divider,
    cursorColor = CH.Accent,
    focusedLabelColor = CH.Accent,
    unfocusedLabelColor = CH.TextSecondary,
    focusedPlaceholderColor = CH.TextSecondary,
    unfocusedPlaceholderColor = CH.TextSecondary,
    focusedContainerColor = CH.Card,
    unfocusedContainerColor = CH.Card
)

@Composable
private fun btnAccent(): ButtonColors =
    ButtonDefaults.buttonColors(containerColor = CH.Accent, contentColor = Color.White)

@Composable
private fun btnPlain(): ButtonColors =
    ButtonDefaults.buttonColors(containerColor = CH.CardAlt, contentColor = CH.TextPrimary)

@Composable
private fun railColors(): NavigationRailItemColors = NavigationRailItemDefaults.colors(
    selectedIconColor = CH.TextPrimary,
    selectedTextColor = CH.TextPrimary,
    indicatorColor = CH.Selected,
    unselectedIconColor = CH.TextSecondary,
    unselectedTextColor = CH.TextSecondary
)

@Composable
private fun LoadingBox() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CH.Accent)
    }
}

@Composable
private fun EmptyBox(msg: String) {
    Box(Modifier.fillMaxSize().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(msg, color = CH.TextSecondary, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TopBar(title: String, onBack: (() -> Unit)?, onSearch: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = CH.TextPrimary,
                modifier = Modifier.size(28.dp).clickable { onBack() }
            )
            Spacer(Modifier.width(16.dp))
        }
        Text(
            title, color = CH.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp, modifier = Modifier.weight(1f),
            textAlign = if (onBack != null) TextAlign.Center else TextAlign.Start
        )
        if (onSearch != null) {
            Icon(
                Icons.Filled.Search, "Search", tint = CH.TextPrimary,
                modifier = Modifier.size(26.dp).clickable { onSearch() }
            )
        } else {
            Spacer(Modifier.size(26.dp))
        }
    }
}

// ---------- brand surfaces ----------

private val BrandBrush = Brush.linearGradient(listOf(CH.GradA, CH.GradB))

private val tileGradients = listOf(
    listOf(Color(0xFF2E74B5), Color(0xFF1B3A6B)),
    listOf(Color(0xFF7A4FE0), Color(0xFF3B2A73)),
    listOf(Color(0xFF12A594), Color(0xFF0C5C55)),
    listOf(Color(0xFFE0577A), Color(0xFF7A2B45)),
    listOf(Color(0xFFE0913B), Color(0xFF7A4E1B))
)

private class HomeTile(
    val label: String, val icon: ImageVector, val section: Section, val grad: List<Color>
)

// ---------- shell + navigation rail ----------

@Composable
fun CarHubShell(vm: MainViewModel) {
    Box(Modifier.fillMaxSize().background(CH.Bg)) {
        Row(Modifier.fillMaxSize()) {
            CarHubRail(vm)
            Column(Modifier.weight(1f).fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (vm.section) {
                        Section.HOME -> HomeSection(vm)
                        Section.MOVIES -> MoviesSection(vm)
                        Section.MUSIC -> MusicSection(vm)
                        Section.GAMES -> GamesSection(vm)
                        Section.KIDS -> KidsSection(vm)
                        Section.CONTENT -> ContentSection(vm)
                        Section.SETTINGS -> SettingsSection(vm)
                    }
                }
                if (vm.nowPlaying != null) NowPlayingBar(vm)
            }
        }
        vm.playing?.let { entry ->
            PlayerOverlay(
                m = entry, startMs = vm.playStartMs, brightness = vm.brightness,
                onBrightness = { vm.updateBrightness(it) },
                onProgress = { pos, dur -> vm.recordResume(entry, pos, dur) },
                onClose = { vm.playing = null }
            )
        }
        if (vm.mode == Mode.PASSENGER && vm.playing == null) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(12.dp).size(42.dp).clip(CircleShape)
                    .background(Color(0x33000000))
                    .pointerInput(Unit) { detectTapGestures(onLongPress = { vm.askExitPin = true }) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Lock, "Hold to exit", tint = CH.TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun NowPlayingBar(vm: MainViewModel) {
    Row(
        Modifier.fillMaxWidth().background(CH.Card).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.MusicNote, null, tint = CH.Accent)
        Spacer(Modifier.width(16.dp))
        Text(
            vm.nowPlaying ?: "", color = CH.TextPrimary, modifier = Modifier.weight(1f),
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Icon(
            if (vm.audioIsPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play/Pause",
            tint = CH.TextPrimary, modifier = Modifier.size(34.dp).clickable { vm.toggleAudio() }
        )
    }
}

@Composable
private fun CarHubRail(vm: MainViewModel) {
    NavigationRail(
        containerColor = CH.Rail,
        header = {
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(BrandBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text("Car", color = CH.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Hub", color = CH.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
        }
    ) {
        RailItem(vm, Section.HOME, Icons.Filled.Home, "Home")
        RailItem(vm, Section.MOVIES, Icons.Filled.Movie, "Movies")
        RailItem(vm, Section.MUSIC, Icons.Filled.MusicNote, "Music")
        RailItem(vm, Section.GAMES, Icons.Filled.SportsEsports, "Games")
        Spacer(Modifier.weight(1f))
        if (vm.mode == Mode.OWNER) {
            NavigationRailItem(
                selected = vm.section == Section.SETTINGS,
                onClick = { vm.go(Section.SETTINGS) },
                icon = { Icon(Icons.Filled.Settings, "Owner") },
                label = { Text("Owner") },
                colors = railColors()
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun RailItem(vm: MainViewModel, s: Section, icon: ImageVector, label: String) {
    NavigationRailItem(
        selected = vm.section == s,
        onClick = { vm.go(s) },
        icon = { Icon(icon, label) },
        label = { Text(label) },
        colors = railColors()
    )
}

// ---------- Home ----------

private fun brightnessIcon(b: Float): ImageVector =
    if (b in 0f..0.2f) Icons.Filled.DarkMode else Icons.Filled.LightMode

@Composable
private fun StatusCluster(vm: MainViewModel) {
    val context = LocalContext.current
    var pct by remember { mutableStateOf<Int?>(null) }
    var charging by remember { mutableStateOf(false) }
    var clock by remember { mutableStateOf("") }
    var bt by remember { mutableStateOf<Boolean?>(null) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, i: android.content.Intent?) {
                if (i == null) return
                val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) pct = level * 100 / scale
                val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
        ContextCompat.registerReceiver(
            context, receiver, IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { try { context.unregisterReceiver(receiver) } catch (e: Exception) { } }
    }
    LaunchedEffect(Unit) {
        while (true) {
            val cal = java.util.Calendar.getInstance()
            var h = cal.get(java.util.Calendar.HOUR)
            if (h == 0) h = 12
            val m = cal.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')
            val ap = if (cal.get(java.util.Calendar.AM_PM) == 0) "AM" else "PM"
            clock = "$h:$m $ap"
            delay(15000)
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            bt = try {
                val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as? android.bluetooth.BluetoothManager
                mgr?.adapter?.isEnabled
            } catch (e: Exception) { null }
            delay(8000)
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(
            brightnessIcon(vm.brightness), "Brightness",
            tint = CH.TextSecondary, modifier = Modifier.size(20.dp).clickable { vm.cycleBrightness() }
        )
        if (clock.isNotEmpty()) Text(clock, color = CH.TextSecondary, fontSize = 14.sp)
        if (bt != null) {
            Icon(
                if (bt == true) Icons.Filled.Bluetooth else Icons.Filled.BluetoothDisabled, "Bluetooth",
                tint = if (bt == true) CH.Accent else CH.TextSecondary, modifier = Modifier.size(18.dp)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (charging) Icons.Filled.Bolt else Icons.Filled.BatteryFull, null,
                tint = if (charging) CH.Accent else CH.TextSecondary, modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(pct?.let { "$it%" } ?: "--", color = CH.TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun HomeSection(vm: MainViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(CH.GradB.copy(alpha = 0.22f), Color.Transparent)))
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(BrandBrush),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Text(
                "CAR HUB", color = CH.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp, modifier = Modifier.weight(1f)
            )
            StatusCluster(vm)
        }
        Column(Modifier.padding(20.dp)) {
            Text(
                if (vm.mode == Mode.PASSENGER) "Enjoy the ride" else "Welcome",
                color = CH.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold
            )
            Text("Choose what to watch, hear or play", color = CH.TextSecondary, fontSize = 15.sp)
            Spacer(Modifier.height(20.dp))

            val tiles = listOf(
                HomeTile("Movies", Icons.Filled.Movie, Section.MOVIES, tileGradients[0]),
                HomeTile("Music", Icons.Filled.MusicNote, Section.MUSIC, tileGradients[1]),
                HomeTile("Games", Icons.Filled.SportsEsports, Section.GAMES, tileGradients[2]),
                HomeTile("Kids", Icons.Filled.ChildCare, Section.KIDS, tileGradients[3])
            )
            tiles.chunked(3).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { t ->
                        FeatureTile(t.label, t.icon, t.grad, Modifier.weight(1f)) { vm.go(t.section) }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            if (vm.continueList.isNotEmpty()) {
                Text("Continue watching", color = CH.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.continueList) { m ->
                        Box(Modifier.width(120.dp)) {
                            PosterCard(m, vm.isFavorite(m.uri), { vm.toggleFavorite(m.uri) }) { vm.openMovie(m) }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            if (vm.mode == Mode.OWNER) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { vm.go(Section.CONTENT) }, colors = btnPlain()) { Text("Manage Content") }
                    Button(onClick = { vm.startPassenger() }, colors = btnAccent()) { Text("Start Passenger Mode") }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Device tier: ${vm.tier}  •  ${if (vm.tier == "A") "Full lockdown" else "Screen-pinning (Tier B)"}",
                    color = CH.TextSecondary, fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun FeatureTile(
    label: String, icon: ImageVector, grad: List<Color>, modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Box(
        modifier.height(130.dp).clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(grad)).clickable { onClick() }
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Color(0x33FFFFFF)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, label, tint = Color.White, modifier = Modifier.size(26.dp)) }
            Text(label, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------- Movies ----------

private val posterGradients = listOf(
    listOf(Color(0xFF3A1C71), Color(0xFF161226)),
    listOf(Color(0xFF134E5E), Color(0xFF12181C)),
    listOf(Color(0xFF8E2DE2), Color(0xFF1A1030)),
    listOf(Color(0xFFCB356B), Color(0xFF2A1220)),
    listOf(Color(0xFF1D4350), Color(0xFF10181B)),
    listOf(Color(0xFFFF512F), Color(0xFF2A140E)),
    listOf(Color(0xFF0F2027), Color(0xFF10161A)),
    listOf(Color(0xFF41295A), Color(0xFF171122))
)

@Composable
private fun MoviesSection(vm: MainViewModel) {
    var category by remember { mutableStateOf("All") }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    val all = vm.movies
    val categories = listOf("All", "★ Favorites") + all.map { it.category }.distinct().sorted()
    val filtered = all.filter {
        val catOk = when (category) {
            "All" -> true
            "★ Favorites" -> vm.isFavorite(it.uri)
            else -> it.category == category
        }
        catOk && (query.isBlank() || it.title.contains(query, ignoreCase = true))
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            "MOVIES",
            onBack = { vm.go(Section.HOME) },
            onSearch = { searching = !searching; if (!searching) query = "" }
        )
        if (searching) {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("Search movies") }, singleLine = true,
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(10.dp))
        }
        if (categories.size > 1) {
            CategoryTabs(categories, category) { category = it }
            Spacer(Modifier.height(8.dp))
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                vm.indexing -> LoadingBox()
                all.isEmpty() -> EmptyBox(
                    "No movies found.\n\nOwner: add video files under your CARHUB folder " +
                        "(e.g. CARHUB/Movies/Telugu), then open Owner → Manage Content and tap Rescan."
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    gridItems(filtered) { m ->
                        PosterCard(m, vm.isFavorite(m.uri), { vm.toggleFavorite(m.uri) }) { vm.openMovie(m) }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryTabs(cats: List<String>, selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        cats.forEach { c ->
            val sel = c == selected
            Box(
                Modifier.clip(RoundedCornerShape(20.dp))
                    .background(if (sel) CH.Selected else Color.Transparent)
                    .clickable { onSelect(c) }
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    c, color = if (sel) CH.TextPrimary else CH.TextSecondary,
                    fontSize = 15.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun rememberThumb(entry: MediaEntry): ImageBitmap? {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(initialValue = Thumbs.cached(entry.uri), entry.uri) {
        if (value == null) value = withContext(Dispatchers.IO) { Thumbs.load(context, entry) }
    }.value
}

@Composable
private fun PosterCard(m: MediaEntry, isFav: Boolean, onToggleFav: () -> Unit, onClick: () -> Unit) {
    val thumb = rememberThumb(m)
    Column(Modifier.clickable { onClick() }) {
        val g = posterGradients[(m.title.hashCode() and 0x7FFFFFFF) % posterGradients.size]
        Box(
            Modifier.fillMaxWidth().aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(g)),
            contentAlignment = Alignment.Center
        ) {
            if (thumb != null) {
                Image(
                    bitmap = thumb, contentDescription = m.title,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    Icons.Filled.Movie, null, tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(48.dp)
                )
            }
            if (m.isMultiPart) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(6.dp)
                        .clip(RoundedCornerShape(6.dp)).background(Color(0xCC000000))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("${m.partUris.size} parts", color = Color.White, fontSize = 11.sp)
                }
            }
            Box(
                Modifier.align(Alignment.TopStart).padding(6.dp)
                    .clip(CircleShape).background(Color(0x88000000))
                    .clickable { onToggleFav() }.padding(5.dp)
            ) {
                Icon(
                    if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, "Favorite",
                    tint = if (isFav) Color(0xFFFF5A79) else Color.White, modifier = Modifier.size(18.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            m.title, color = CH.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        if (m.durationLabel.isNotEmpty()) {
            Text(m.durationLabel, color = CH.TextSecondary, fontSize = 13.sp)
        }
    }
}

// ---------- Music ----------

@Composable
private fun MusicSection(vm: MainViewModel) {
    Column(Modifier.fillMaxSize()) {
        TopBar("MUSIC", onBack = { vm.go(Section.HOME) })
        Box(Modifier.weight(1f).fillMaxWidth()) {
            val songs = vm.songs
            when {
                vm.indexing -> LoadingBox()
                songs.isEmpty() -> EmptyBox(
                    "No music found.\n\nAdd audio files under your CARHUB folder " +
                        "(e.g. CARHUB/Music/Telugu), then Rescan in Owner → Manage Content."
                )
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(songs) { s ->
                        Row(
                            Modifier.fillMaxWidth().clickable { vm.playAudio(s) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val art = rememberThumb(s)
                            Box(
                                Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(CH.CardAlt),
                                contentAlignment = Alignment.Center
                            ) {
                                if (art != null) {
                                    Image(
                                        bitmap = art, contentDescription = null,
                                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Filled.MusicNote, null, tint = CH.Accent, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(s.title, color = CH.TextPrimary, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (s.category.isNotBlank()) {
                                    Text(s.category, color = CH.TextSecondary, fontSize = 12.sp)
                                }
                            }
                            if (s.durationLabel.isNotEmpty()) {
                                Text(s.durationLabel, color = CH.TextSecondary, fontSize = 13.sp)
                            }
                        }
                        HorizontalDivider(color = CH.Divider)
                    }
                }
            }
        }
    }
}

// ---------- Video player overlay ----------

@Composable
private fun PlayerOverlay(
    m: MediaEntry, startMs: Long, brightness: Float,
    onBrightness: (Float) -> Unit, onProgress: (Long, Long) -> Unit, onClose: () -> Unit
) {
    val context = LocalContext.current
    val audio = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVol = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }
    var vol by remember { mutableStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC)) }
    var speed by remember { mutableStateOf(1f) }
    var error by remember { mutableStateOf<String?>(null) }
    val speeds = listOf(0.5f, 1f, 1.25f, 1.5f, 2f)
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val items = m.playUris.mapIndexed { i, u ->
                val b = MediaItem.Builder().setUri(Uri.parse(u))
                if (i == 0 && m.subtitleUri != null) {
                    val cfg = MediaItem.SubtitleConfiguration.Builder(Uri.parse(m.subtitleUri))
                        .setMimeType(subMime(m.subtitleExt))
                        .setLanguage("und")
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                    b.setSubtitleConfigurations(listOf(cfg))
                }
                b.build()
            }
            setMediaItems(items)
            addListener(object : Player.Listener {
                override fun onPlayerError(e: PlaybackException) {
                    error = "This file's format isn't supported on this device."
                }
            })
            prepare()
            if (startMs > 0) seekTo(startMs)
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            try { onProgress(player.currentPosition, player.duration.coerceAtLeast(0L)) } catch (e: Exception) { }
            player.release()
        }
    }

    val speedLabel = when (speed) {
        0.5f -> "0.5x"; 1.25f -> "1.25x"; 1.5f -> "1.5x"; 2f -> "2x"; else -> "1x"
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true } },
            modifier = Modifier.fillMaxSize()
        )
        Column(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xAA000000), Color.Transparent)))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White,
                    modifier = Modifier.size(30.dp).clickable { onClose() }
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    m.title + if (m.isMultiPart) "  (${m.partUris.size} parts)" else "",
                    color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0x55FFFFFF))
                        .clickable {
                            speed = speeds[(speeds.indexOf(speed) + 1) % speeds.size]
                            player.setPlaybackSpeed(speed)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(speedLabel, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LightMode, "Brightness", tint = Color.White, modifier = Modifier.size(20.dp))
                Slider(
                    value = if (brightness < 0f) 1f else brightness,
                    onValueChange = { onBrightness(it.coerceIn(0.05f, 1f)) },
                    valueRange = 0.05f..1f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Filled.VolumeUp, "Volume", tint = Color.White, modifier = Modifier.size(20.dp))
                Slider(
                    value = vol.toFloat(),
                    onValueChange = { v ->
                        vol = v.toInt()
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
                    },
                    valueRange = 0f..maxVol.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
            }
        }
        if (error != null) {
            Box(
                Modifier.fillMaxSize().background(Color(0xE6000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(Icons.Filled.Movie, null, tint = CH.TextSecondary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(error!!, color = CH.TextPrimary, fontSize = 18.sp, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Try an MP4/H.264 version, or a different file.",
                        color = CH.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(onClick = { onClose() }, colors = btnAccent()) { Text("Back") }
                }
            }
        }
    }
}

private fun subMime(ext: String?): String = when (ext?.lowercase()) {
    "vtt" -> MimeTypes.TEXT_VTT
    "ass", "ssa" -> MimeTypes.TEXT_SSA
    else -> MimeTypes.APPLICATION_SUBRIP
}

// ---------- Games ----------

@Composable
private fun GamesSection(vm: MainViewModel) {
    Column(Modifier.fillMaxSize()) {
        TopBar("GAMES", onBack = { vm.go(Section.HOME) })
        Box(Modifier.weight(1f).fillMaxWidth()) { TicTacToe() }
    }
}

@Composable
private fun TicTacToe() {
    var board by remember { mutableStateOf(List(9) { "" }) }
    var status by remember { mutableStateOf("Your turn (X)") }
    var over by remember { mutableStateOf(false) }

    fun winnerOf(b: List<String>): String? {
        val lines = listOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
        for (l in lines) {
            if (b[l[0]].isNotEmpty() && b[l[0]] == b[l[1]] && b[l[1]] == b[l[2]]) return b[l[0]]
        }
        return null
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(status, color = CH.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        for (r in 0..2) {
            Row {
                for (c in 0..2) {
                    val i = r * 3 + c
                    Box(
                        Modifier.padding(6.dp).size(88.dp).clip(RoundedCornerShape(12.dp))
                            .background(CH.Card)
                            .clickable(enabled = !over && board[i].isEmpty()) {
                                val nb = board.toMutableList(); nb[i] = "X"
                                var b = nb.toList()
                                if (winnerOf(b) != null) { board = b; status = "You win! 🎉"; over = true; return@clickable }
                                if (b.none { it.isEmpty() }) { board = b; status = "Draw"; over = true; return@clickable }
                                val empty = b.indices.filter { b[it].isEmpty() }
                                val move = empty[(b.hashCode() and 0x7FFFFFFF) % empty.size]
                                val nb2 = b.toMutableList(); nb2[move] = "O"; b = nb2.toList()
                                board = b
                                when {
                                    winnerOf(b) != null -> { status = "Computer wins"; over = true }
                                    b.none { it.isEmpty() } -> { status = "Draw"; over = true }
                                    else -> status = "Your turn (X)"
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            board[i],
                            color = if (board[i] == "X") CH.Accent else CH.TextPrimary,
                            fontSize = 44.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { board = List(9) { "" }; status = "Your turn (X)"; over = false },
            colors = btnAccent()
        ) { Text("New Game") }
    }
}

// ---------- Maps / Kids placeholders ----------

@Composable
private fun KidsSection(vm: MainViewModel) {
    val kidMovies = vm.kidsMovies()
    Column(Modifier.fillMaxSize()) {
        TopBar("KIDS", onBack = { vm.go(Section.HOME) })
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                vm.kidsCategories.isEmpty() -> EmptyBox(
                    "Kids Mode is empty.\n\nOwner: open Owner → Settings → Kids-safe folders and pick which categories kids can see."
                )
                kidMovies.isEmpty() -> EmptyBox("No kid-safe movies in the selected folders yet.")
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    gridItems(kidMovies) { m ->
                        PosterCard(m, vm.isFavorite(m.uri), { vm.toggleFavorite(m.uri) }) { vm.openMovie(m) }
                    }
                }
            }
        }
    }
}

// ---------- Owner: Content manager ----------

@Composable
private fun ContentSection(vm: MainViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) { }
            vm.setTree(uri)
        }
    }
    Column(Modifier.fillMaxSize()) {
        TopBar("MANAGE CONTENT", onBack = { vm.go(Section.HOME) })
        Column(
            Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("CARHUB folder", color = CH.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(vm.treeUri ?: "Not selected yet", color = CH.TextSecondary, fontSize = 13.sp)
            Button(onClick = { launcher.launch(null) }, colors = btnAccent()) { Text("Select CARHUB folder") }
            HorizontalDivider(color = CH.Divider)
            Text("Library", color = CH.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Movies: ${vm.movies.size}", color = CH.TextPrimary, fontSize = 16.sp)
            Text("Songs: ${vm.songs.size}", color = CH.TextPrimary, fontSize = 16.sp)
            if (vm.indexing) Text("Scanning…", color = CH.Accent, fontSize = 14.sp)
            Button(onClick = { vm.rescan() }, colors = btnPlain()) { Text("Rescan") }
        }
    }
}

// ---------- Owner: Settings ----------

@Composable
private fun SettingsSection(vm: MainViewModel) {
    var pin by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        TopBar("OWNER SETTINGS", onBack = { vm.go(Section.HOME) })
        Column(
            Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Device capability tier: ${vm.tier}", color = CH.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                if (vm.tier == "A") "Device Owner active — full kiosk lockdown available."
                else "No Device Owner. Passenger Mode uses screen pinning (Tier B). " +
                    "Full lockdown needs one-time provisioning — see the build guide.",
                color = CH.TextSecondary, fontSize = 13.sp
            )
            HorizontalDivider(color = CH.Divider)
            Text("Screen brightness", color = CH.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Tap the brightness icon on Home to cycle Day → Dim → Night.", color = CH.TextSecondary, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.DarkMode, null, tint = CH.TextSecondary, modifier = Modifier.size(20.dp))
                Slider(
                    value = if (vm.brightness < 0f) 1f else vm.brightness,
                    onValueChange = { vm.updateBrightness(it.coerceIn(0.05f, 1f)) },
                    valueRange = 0.05f..1f,
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Icon(Icons.Filled.LightMode, null, tint = CH.TextSecondary, modifier = Modifier.size(20.dp))
            }
            Button(onClick = { vm.updateBrightness(-1f) }, colors = btnPlain()) { Text("Use system brightness") }
            HorizontalDivider(color = CH.Divider)
            Text("Change Owner PIN", color = CH.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            OutlinedTextField(
                value = pin,
                onValueChange = { pin = it.filter { ch -> ch.isDigit() }.take(8) },
                label = { Text("New PIN (4–8 digits)") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true, colors = fieldColors()
            )
            Button(onClick = {
                if (pin.length in 4..8) { vm.changePin(pin); pin = ""; msg = "PIN updated." }
                else msg = "PIN must be 4–8 digits."
            }, colors = btnAccent()) { Text("Update PIN") }
            if (msg.isNotEmpty()) Text(msg, color = CH.Accent, fontSize = 14.sp)
            HorizontalDivider(color = CH.Divider)
            Text("Kids-safe folders", color = CH.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Only these categories appear in the Kids section.", color = CH.TextSecondary, fontSize = 13.sp)
            if (vm.allCategories.isEmpty()) {
                Text("No content yet — add media and rescan.", color = CH.TextSecondary, fontSize = 13.sp)
            } else {
                vm.allCategories.forEach { cat ->
                    Row(
                        Modifier.fillMaxWidth().clickable { vm.toggleKidsCategory(cat) }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (cat in vm.kidsCategories) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                            null, tint = if (cat in vm.kidsCategories) CH.Accent else CH.TextSecondary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(cat, color = CH.TextPrimary, fontSize = 16.sp)
                    }
                }
            }
            HorizontalDivider(color = CH.Divider)
            Button(onClick = { vm.startPassenger() }, colors = btnPlain()) { Text("Start Passenger Mode") }
        }
    }
}

// ---------- First-run + lock screens ----------

@Composable
private fun PinPad(title: String, error: String?, submitLabel: String, onSubmit: (String) -> Unit, onCancel: (() -> Unit)?) {
    var pin by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = CH.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(maxOf(4, pin.length)) { i ->
                Box(
                    Modifier.size(12.dp).clip(CircleShape)
                        .background(if (i < pin.length) CH.Accent else CH.CardAlt)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        if (error != null) {
            Text(error, color = Color(0xFFEF5350), fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
        }
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫").chunked(3).forEach { row ->
            Row {
                row.forEach { k ->
                    Box(
                        Modifier.padding(8.dp).size(62.dp).clip(CircleShape)
                            .background(if (k.isEmpty()) Color.Transparent else CH.Card)
                            .then(
                                if (k.isEmpty()) Modifier
                                else Modifier.clickable {
                                    if (k == "⌫") { if (pin.isNotEmpty()) pin = pin.dropLast(1) }
                                    else if (pin.length < 8) pin += k
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (k.isNotEmpty()) {
                            Text(k, color = CH.TextPrimary, fontSize = if (k == "⌫") 22.sp else 26.sp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (onCancel != null) Button(onClick = onCancel, colors = btnPlain()) { Text("Cancel") }
            Button(onClick = { onSubmit(pin) }, colors = btnAccent(), enabled = pin.length >= 4) {
                Text(submitLabel)
            }
        }
    }
}

@Composable
private fun PatternLock(title: String, error: String?, onPattern: (List<Int>) -> Unit, onCancel: (() -> Unit)?) {
    var selected by remember { mutableStateOf<List<Int>>(emptyList()) }
    var current by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = CH.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        if (error != null) {
            Text(error, color = Color(0xFFEF5350), fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
        }
        Canvas(
            Modifier.size(260.dp).pointerInput(Unit) {
                val cell = size.width / 3f
                fun centerOf(i: Int) = Offset((i % 3 + 0.5f) * cell, (i / 3 + 0.5f) * cell)
                fun hit(pos: Offset): Int? {
                    for (i in 0 until 9) if ((pos - centerOf(i)).getDistance() < cell * 0.30f) return i
                    return null
                }
                detectDragGestures(
                    onDragStart = { pos ->
                        dragging = true; selected = emptyList()
                        hit(pos)?.let { selected = listOf(it) }; current = pos
                    },
                    onDrag = { change, _ ->
                        current = change.position
                        hit(change.position)?.let { if (it !in selected) selected = selected + it }
                    },
                    onDragEnd = {
                        dragging = false
                        val done = selected; selected = emptyList()
                        if (done.size >= 4) onPattern(done)
                    },
                    onDragCancel = { dragging = false; selected = emptyList() }
                )
            }
        ) {
            val cell = size.width / 3f
            fun centerOf(i: Int) = Offset((i % 3 + 0.5f) * cell, (i / 3 + 0.5f) * cell)
            for (k in 0 until selected.size - 1) {
                drawLine(CH.Accent, centerOf(selected[k]), centerOf(selected[k + 1]), strokeWidth = 12f)
            }
            if (dragging && selected.isNotEmpty()) {
                drawLine(CH.Accent, centerOf(selected.last()), current, strokeWidth = 12f)
            }
            for (i in 0 until 9) {
                drawCircle(CH.CardAlt, radius = cell * 0.12f, center = centerOf(i))
                if (i in selected) drawCircle(CH.Accent, radius = cell * 0.16f, center = centerOf(i))
            }
        }
        Spacer(Modifier.height(12.dp))
        if (onCancel != null) Button(onClick = onCancel, colors = btnPlain()) { Text("Cancel") }
    }
}

@Composable
fun ExitGate(
    lockedSeconds: Long,
    verifyPin: suspend (String) -> Boolean,
    verifyPattern: suspend (List<Int>) -> Boolean,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxSize().background(CH.Bg), contentAlignment = Alignment.Center) {
        if (lockedSeconds > 0) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Lock, null, tint = CH.TextSecondary, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text("Too many attempts", color = CH.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Try again in ${lockedSeconds}s", color = CH.TextSecondary, fontSize = 14.sp)
                Spacer(Modifier.height(20.dp))
                Button(onClick = onCancel, colors = btnPlain()) { Text("Back") }
            }
        } else if (step == 0) {
            PinPad("Enter Owner PIN", err, "Next", onSubmit = { pin ->
                scope.launch { if (verifyPin(pin)) { err = null; step = 1 } else err = "Incorrect PIN" }
            }, onCancel = onCancel)
        } else {
            PatternLock("Draw your unlock pattern", err, onPattern = { seq ->
                scope.launch { if (verifyPattern(seq)) onSuccess() else err = "Pattern doesn't match" }
            }, onCancel = { step = 0; err = null })
        }
    }
}

@Composable
fun SplashScreen() {
    val transition = rememberInfiniteTransition(label = "splash")
    val scale by transition.animateFloat(
        initialValue = 0.94f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "scale"
    )
    Box(
        Modifier.fillMaxSize()
            .background(Brush.radialGradient(listOf(CH.Glow, CH.Bg), radius = 1100f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(100.dp).scale(scale).clip(RoundedCornerShape(28.dp)).background(BrandBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(54.dp))
            }
            Spacer(Modifier.height(22.dp))
            Text("CAR HUB", color = CH.TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp)
            Spacer(Modifier.height(6.dp))
            Text("in-car entertainment", color = CH.TextSecondary, fontSize = 13.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun PinSetupScreen(onComplete: (String, List<Int>) -> Unit) {
    var step by remember { mutableStateOf(0) }
    var pin1 by remember { mutableStateOf("") }
    var pat1 by remember { mutableStateOf<List<Int>>(emptyList()) }
    var err by remember { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize().background(CH.Bg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Set up Car Hub", color = CH.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Step ${step + 1} of 4", color = CH.TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(20.dp))
            when (step) {
                0 -> PinPad(
                    "Create a PIN (4–8 digits)", err, "Next",
                    onSubmit = { pin1 = it; err = null; step = 1 }, onCancel = null
                )
                1 -> PinPad(
                    "Confirm your PIN", err, "Next",
                    onSubmit = { if (it == pin1) { err = null; step = 2 } else err = "PINs don't match" },
                    onCancel = { step = 0; err = null }
                )
                2 -> PatternLock(
                    "Draw an unlock pattern (4+ dots)", err,
                    onPattern = { pat1 = it; err = null; step = 3 }, onCancel = null
                )
                else -> PatternLock(
                    "Draw the pattern again", err,
                    onPattern = { if (it == pat1) onComplete(pin1, pat1) else err = "Patterns don't match" },
                    onCancel = { step = 2; err = null }
                )
            }
        }
    }
}
