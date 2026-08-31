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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
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

// ---------- shell + navigation rail ----------

@Composable
fun CarHubShell(vm: MainViewModel) {
    Box(Modifier.fillMaxSize().background(CH.Bg)) {
        Row(Modifier.fillMaxSize()) {
            CarHubRail(vm)
            Box(Modifier.weight(1f).fillMaxSize()) {
                when (vm.section) {
                    Section.HOME -> HomeSection(vm)
                    Section.MOVIES -> MoviesSection(vm)
                    Section.MUSIC -> MusicSection(vm)
                    Section.GAMES -> GamesSection(vm)
                    Section.MAPS -> MapsSection(vm)
                    Section.KIDS -> KidsSection(vm)
                    Section.CONTENT -> ContentSection(vm)
                    Section.SETTINGS -> SettingsSection(vm)
                }
            }
        }
        vm.playing?.let { entry ->
            PlayerOverlay(entry) { vm.playing = null }
        }
    }
}

@Composable
private fun CarHubRail(vm: MainViewModel) {
    NavigationRail(
        containerColor = CH.Rail,
        header = {
            Spacer(Modifier.height(14.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Car", color = CH.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Hub", color = CH.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
        }
    ) {
        RailItem(vm, Section.HOME, Icons.Filled.Home, "Home")
        RailItem(vm, Section.MOVIES, Icons.Filled.Movie, "Movies")
        RailItem(vm, Section.MUSIC, Icons.Filled.MusicNote, "Music")
        RailItem(vm, Section.GAMES, Icons.Filled.SportsEsports, "Games")
        RailItem(vm, Section.MAPS, Icons.Filled.Map, "Maps")
        Spacer(Modifier.weight(1f))
        if (vm.mode == Mode.PASSENGER) {
            NavigationRailItem(
                selected = false,
                onClick = { vm.askExitPin = true },
                icon = { Icon(Icons.Filled.Lock, "Exit") },
                label = { Text("Exit") },
                colors = railColors()
            )
        } else {
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

@Composable
private fun HomeSection(vm: MainViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopBar("CAR HUB", onBack = null)
        Column(Modifier.padding(20.dp)) {
            Text(
                if (vm.mode == Mode.PASSENGER) "Enjoy the ride" else "Welcome",
                color = CH.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold
            )
            Text("Choose what to watch, hear or play", color = CH.TextSecondary, fontSize = 15.sp)
            Spacer(Modifier.height(20.dp))

            val tiles = listOf(
                Triple("Movies", Icons.Filled.Movie, Section.MOVIES),
                Triple("Music", Icons.Filled.MusicNote, Section.MUSIC),
                Triple("Games", Icons.Filled.SportsEsports, Section.GAMES),
                Triple("Maps", Icons.Filled.Map, Section.MAPS),
                Triple("Kids", Icons.Filled.ChildCare, Section.KIDS)
            )
            tiles.chunked(3).forEach { row ->
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    row.forEach { (label, icon, sec) ->
                        FeatureTile(label, icon, Modifier.weight(1f)) { vm.go(sec) }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
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
private fun FeatureTile(label: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(120.dp).clip(RoundedCornerShape(16.dp)).background(CH.Card).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, label, tint = CH.Accent, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(10.dp))
            Text(label, color = CH.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
    val categories = listOf("All") + all.map { it.category }.distinct().sorted()
    val filtered = all.filter {
        (category == "All" || it.category == category) &&
            (query.isBlank() || it.title.contains(query, ignoreCase = true))
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
                    gridItems(filtered) { m -> PosterCard(m) { vm.playing = m } }
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
private fun PosterCard(m: MediaEntry, onClick: () -> Unit) {
    Column(Modifier.clickable { onClick() }) {
        val g = posterGradients[(m.title.hashCode() and 0x7FFFFFFF) % posterGradients.size]
        Box(
            Modifier.fillMaxWidth().aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(g)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Movie, null, tint = Color.White.copy(alpha = 0.25f),
                modifier = Modifier.size(48.dp)
            )
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
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    var nowPlaying by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    DisposableEffect(Unit) { onDispose { player.release() } }

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
                            Modifier.fillMaxWidth().clickable {
                                player.setMediaItem(MediaItem.fromUri(Uri.parse(s.uri)))
                                player.prepare(); player.play()
                                nowPlaying = s.title; isPlaying = true
                            }.padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.MusicNote, null, tint = CH.Accent, modifier = Modifier.size(24.dp))
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
        if (nowPlaying != null) {
            Row(
                Modifier.fillMaxWidth().background(CH.Card).padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.MusicNote, null, tint = CH.Accent)
                Spacer(Modifier.width(16.dp))
                Text(nowPlaying!!, color = CH.TextPrimary, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, "Play/Pause",
                    tint = CH.TextPrimary,
                    modifier = Modifier.size(36.dp).clickable {
                        if (isPlaying) player.pause() else player.play()
                        isPlaying = !isPlaying
                    }
                )
            }
        }
    }
}

// ---------- Video player overlay ----------

@Composable
private fun PlayerOverlay(m: MediaEntry, onClose: () -> Unit) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(m.uri)))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx -> PlayerView(ctx).apply { this.player = player; useController = true } },
            modifier = Modifier.fillMaxSize()
        )
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White,
                modifier = Modifier.size(30.dp).clickable { onClose() }
            )
            Spacer(Modifier.width(16.dp))
            Text(m.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
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
private fun MapsSection(vm: MainViewModel) {
    Column(Modifier.fillMaxSize()) {
        TopBar("MAPS", onBack = { vm.go(Section.HOME) })
        Box(Modifier.weight(1f).fillMaxWidth()) {
            EmptyBox(
                "Offline maps arrive in the next build.\n\nThey will render OpenStreetMap data placed " +
                    "in CARHUB/Maps — no internet required."
            )
        }
    }
}

@Composable
private fun KidsSection(vm: MainViewModel) {
    Column(Modifier.fillMaxSize()) {
        TopBar("KIDS", onBack = { vm.go(Section.HOME) })
        Box(Modifier.weight(1f).fillMaxWidth()) {
            EmptyBox(
                "Kids Mode will show only owner-approved movies, music and games.\n\nComing in a future build."
            )
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
            Button(onClick = { vm.startPassenger() }, colors = btnPlain()) { Text("Start Passenger Mode") }
        }
    }
}

// ---------- First-run + lock screens ----------

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(CH.Bg), contentAlignment = Alignment.Center) {
        Text("CAR HUB", color = CH.TextPrimary, fontSize = 40.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
    }
}

@Composable
fun PinSetupScreen(onDone: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var err by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().background(CH.Bg).padding(40.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Car Hub", color = CH.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Create an Owner PIN. You'll use it to exit Passenger Mode.", color = CH.TextSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            pin, { pin = it.filter { c -> c.isDigit() }.take(8) }, label = { Text("PIN (4–8 digits)") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true, colors = fieldColors()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            confirm, { confirm = it.filter { c -> c.isDigit() }.take(8) }, label = { Text("Confirm PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true, colors = fieldColors()
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            when {
                pin.length !in 4..8 -> err = "PIN must be 4–8 digits."
                pin != confirm -> err = "PINs do not match."
                else -> onDone(pin)
            }
        }, colors = btnAccent()) { Text("Create PIN", fontSize = 16.sp) }
        if (err.isNotEmpty()) { Spacer(Modifier.height(12.dp)); Text(err, color = Color(0xFFEF5350)) }
    }
}

@Composable
fun PinEntryScreen(
    title: String,
    onVerify: suspend (String) -> Boolean,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var err by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize().background(CH.Bg).padding(40.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = CH.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            pin, { pin = it.filter { c -> c.isDigit() }.take(8) }, label = { Text("Owner PIN") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            singleLine = true, colors = fieldColors()
        )
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onCancel() }, colors = btnPlain()) { Text("Cancel") }
            Button(onClick = {
                scope.launch { if (onVerify(pin)) onSuccess() else { err = "Incorrect PIN"; pin = "" } }
            }, colors = btnAccent()) { Text("Unlock") }
        }
        if (err.isNotEmpty()) { Spacer(Modifier.height(12.dp)); Text(err, color = Color(0xFFEF5350)) }
    }
}
