package com.nexa.mobile.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.GppMaybe
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.nexa.mobile.NexaSettings
import com.nexa.mobile.StreamQuality
import com.nexa.mobile.controller.ControllerPad
import com.nexa.mobile.controller.UnavailableInputBridge
import com.nexa.mobile.games.InstalledApps
import com.nexa.mobile.games.LaunchableApp
import com.nexa.mobile.net.TvDiscovery
import com.nexa.mobile.net.TvLink
import com.nexa.mobile.nexa
import com.nexa.mobile.stream.StreamingService
import com.nexa.protocol.PairingCode
import com.nexa.protocol.PairingUri
import com.nexa.ui.Eyebrow
import com.nexa.ui.LogoSize
import com.nexa.ui.NexaColors
import com.nexa.ui.NexaLogo
import com.nexa.ui.PlaceholderBadge
import com.nexa.ui.StatusDot
import com.nexa.ui.nexaCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class Screen { Home, Connect, Games, Editor, Settings }

@Composable
fun MobileApp() {
    val context = LocalContext.current
    val app = context.nexa
    var screen by rememberSaveable { mutableStateOf(Screen.Home) }
    var editorTarget by remember { mutableStateOf<LaunchableApp?>(null) }
    val linkState by app.link.state.collectAsState()
    val session by app.session.collectAsState()

    BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

    if (screen == Screen.Editor) {
        ControllerEditor(target = editorTarget, onClose = { screen = if (editorTarget == null) Screen.Home else Screen.Games })
        return
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(NexaColors.Obsidian)
            .safeDrawingPadding()
    ) {
        when (screen) {
            Screen.Home -> HomeScreen(
                linkState = linkState,
                sessionLabel = session?.appLabel,
                onConnect = { screen = Screen.Connect },
                onGames = { screen = Screen.Games },
                onSettings = { screen = Screen.Settings },
                onEditDefaultLayout = { editorTarget = null; screen = Screen.Editor },
                onEndSession = { StreamingService.stop(context) },
            )
            Screen.Connect -> ConnectScreen(linkState, onBack = { screen = Screen.Home }, onGames = { screen = Screen.Games })
            Screen.Games -> GamesScreen(
                linkState = linkState,
                onBack = { screen = Screen.Home },
                onNeedConnection = { screen = Screen.Connect },
                onEditLayout = { editorTarget = it; screen = Screen.Editor },
            )
            Screen.Settings -> SettingsScreen(onBack = { screen = Screen.Home })
            Screen.Editor -> Unit
        }
    }
}

// ----------------------------------------------------------------------------
// TELA PRINCIPAL (Gemini "MOBILE SCREEN 2"; login/perfil removidos: V1 sem contas)
// ----------------------------------------------------------------------------
@Composable
private fun HomeScreen(
    linkState: TvLink.State,
    sessionLabel: String?,
    onConnect: () -> Unit,
    onGames: () -> Unit,
    onSettings: () -> Unit,
    onEditDefaultLayout: () -> Unit,
    onEndSession: () -> Unit,
) {
    val context = LocalContext.current
    val connected = linkState as? TvLink.State.Connected
    val appCount by produceState<Int?>(null) {
        value = withContext(Dispatchers.IO) { InstalledApps.load(context).size }
    }

    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NexaLogo(LogoSize.Medium, showTagline = true)
            Spacer(Modifier.weight(1f))
            IconSquare(Icons.Outlined.Settings, onSettings)
        }

        // Status da TV + CTA
        Column(Modifier.fillMaxWidth().nexaCard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Status da TV", fontSize = 12)
                Spacer(Modifier.weight(1f))
                Pill(if (connected != null) "TV Conectada" else "Desconectado", active = connected != null)
            }
            Column {
                Text(
                    connected?.tvName ?: "Nenhuma TV pareada",
                    color = NexaColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    when {
                        sessionLabel != null -> "Transmitindo $sessionLabel para a TV."
                        connected != null -> "Pronto para transmitir pela rede local."
                        else -> "Escaneie o QR Code exibido no aplicativo NEXA TV."
                    },
                    color = NexaColors.TextMuted, fontSize = 13.sp,
                )
            }
            if (sessionLabel != null) {
                PrimaryButton("Encerrar sessão", Icons.Outlined.PowerSettingsNew, onEndSession)
            } else {
                PrimaryButton(
                    if (connected != null) "Gerenciar Conexão TV" else "Conectar à TV",
                    Icons.Outlined.QrCodeScanner, onConnect,
                )
            }
        }

        // Meus Jogos
        Row(
            Modifier.fillMaxWidth().nexaCard().clickable(onClick = onGames).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(44.dp).nexaCard(12.dp, NexaColors.Violet.copy(alpha = 0.4f), NexaColors.Obsidian),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Outlined.SportsEsports, null, tint = NexaColors.VioletGlow, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Meus Jogos", color = NexaColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    appCount?.let { "$it apps instalados detectados" } ?: "Carregando apps…",
                    color = NexaColors.TextMuted, fontSize = 13.sp,
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = NexaColors.TextMuted)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SecondaryButton("Configurações", Icons.Outlined.Settings, onSettings, Modifier.weight(1f))
            SecondaryButton("Controle", Icons.Outlined.Tune, onEditDefaultLayout, Modifier.weight(1f))
        }
    }
}

// ----------------------------------------------------------------------------
// CONEXÃO (QR & MANUAL) (Gemini "MOBILE SCREEN 3")
// ----------------------------------------------------------------------------
@Composable
private fun ConnectScreen(linkState: TvLink.State, onBack: () -> Unit, onGames: () -> Unit) {
    val context = LocalContext.current
    val link = context.nexa.link
    val scope = rememberCoroutineScope()
    var manualCode by rememberSaveable { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val busy = linkState is TvLink.State.Connecting
    val connected = linkState as? TvLink.State.Connected

    fun scanQr() {
        localError = null
        val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        GmsBarcodeScanning.getClient(context, options).startScan()
            .addOnSuccessListener { barcode ->
                val info = PairingUri.parse(barcode.rawValue.orEmpty())
                if (info == null) {
                    localError = "Este QR Code não é de uma NEXA TV."
                } else {
                    scope.launch { link.connect(info.host, info.port, token = info.token) }
                }
            }
            .addOnFailureListener { e -> localError = "Leitor de QR indisponível: ${e.message}. Use o código manual." }
    }

    fun connectWithCode() {
        localError = null
        val code = PairingCode.normalize(manualCode)
        if (code == null) {
            localError = "Código inválido. Formato: NX-XXXX"
            return
        }
        scope.launch {
            link.setStatus(TvLink.State.Connecting("Procurando NEXA TV na rede…"))
            val candidates = TvDiscovery(context).find()
            if (candidates.isEmpty()) {
                link.setStatus(TvLink.State.Failed("Nenhuma NEXA TV encontrada nesta rede. Use o QR Code."))
                return@launch
            }
            for (c in candidates) {
                if (link.connect(c.host, c.port, code = code).isSuccess) break
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Header("Conectar à TV", onBack)

        // Visor do QR (Gemini) — toque para abrir o leitor
        Column(
            Modifier.fillMaxWidth().nexaCard(background = Color(0xFF050507)).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(160.dp).clickable(enabled = !busy) { scanQr() }, contentAlignment = Alignment.Center) {
                ViewfinderCorners()
                Box(
                    Modifier.width(124.dp).height(2.dp)
                        .shadow(8.dp, ambientColor = NexaColors.VioletGlow, spotColor = NexaColors.VioletGlow)
                        .background(NexaColors.Violet)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (connected != null) "QR Code Validado ✓" else "Escanear QR Code",
                color = NexaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(NexaColors.Violet)
                    .clickable(enabled = !busy) { scanQr() }.padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }

        // Código manual
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ou insira o código exibido na TV:", color = NexaColors.TextMuted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it.uppercase().take(7) },
                    singleLine = true,
                    textStyle = TextStyle(color = NexaColors.Text, fontFamily = FontFamily.Monospace, fontSize = 14.sp, letterSpacing = 3.sp),
                    cursorBrush = SolidColor(NexaColors.Violet),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { connectWithCode() }),
                    modifier = Modifier.weight(1f).nexaCard(12.dp).padding(horizontal = 12.dp, vertical = 11.dp),
                    decorationBox = { inner ->
                        if (manualCode.isEmpty()) {
                            Text("NX-8K4Q", color = NexaColors.TextMuted.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 14.sp, letterSpacing = 3.sp)
                        }
                        inner()
                    },
                )
                Text(
                    "Conectar",
                    color = NexaColors.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(NexaColors.Border)
                        .clickable(enabled = !busy) { connectWithCode() }.padding(horizontal = 14.dp, vertical = 11.dp),
                )
            }
        }

        // Estado da conexão
        Row(Modifier.fillMaxWidth().nexaCard(12.dp).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (connected != null) "TV Conectada" else "Estado da Conexão", color = NexaColors.TextMuted, fontSize = 11.sp)
                Text(
                    localError ?: when (linkState) {
                        is TvLink.State.Connected -> linkState.tvName
                        is TvLink.State.Connecting -> linkState.message
                        is TvLink.State.Failed -> linkState.message
                        TvLink.State.Disconnected -> "Aguardando código..."
                    },
                    color = NexaColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                )
            }
            if (connected != null) {
                SmallAction("Desconectar", primary = false) { link.disconnect() }
                Spacer(Modifier.width(6.dp))
                SmallAction("Ir aos Jogos", primary = true, onClick = onGames)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// MEUS JOGOS (Gemini "MOBILE SCREEN 4") + modal de autorização de captura
// ----------------------------------------------------------------------------
@Composable
private fun GamesScreen(
    linkState: TvLink.State,
    onBack: () -> Unit,
    onNeedConnection: () -> Unit,
    onEditLayout: (LaunchableApp) -> Unit,
) {
    val context = LocalContext.current
    val apps by produceState<List<LaunchableApp>?>(null) {
        value = withContext(Dispatchers.IO) { InstalledApps.load(context) }
    }
    var onlyGames by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var consentFor by remember { mutableStateOf<LaunchableApp?>(null) }
    val startSession = rememberSessionStarter()
    val connected = linkState is TvLink.State.Connected

    val all = apps.orEmpty()
    val hasGames = all.any { it.isGame }
    val showGames = onlyGames ?: hasGames
    val visible = if (showGames) all.filter { it.isGame } else all

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconSquare(Icons.AutoMirrored.Filled.KeyboardArrowLeft, onBack)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Meus Jogos", color = NexaColors.Text, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (connected) "Conectado à TV • Pronto" else "Conecte à TV para jogar",
                        color = NexaColors.Lavender, fontSize = 11.sp,
                    )
                }
                Text(
                    "${visible.size} APPS",
                    color = NexaColors.TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                    modifier = Modifier.nexaCard(4.dp).padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip("Jogos", showGames) { onlyGames = true }
                FilterChip("Todos os apps", !showGames) { onlyGames = false }
            }
            Spacer(Modifier.height(10.dp))

            when {
                apps == null -> Text("Carregando apps instalados…", color = NexaColors.TextMuted, fontSize = 13.sp)
                visible.isEmpty() -> Text(
                    if (showGames) "Nenhum app declarado como jogo. Veja \"Todos os apps\"." else "Nenhum app encontrado.",
                    color = NexaColors.TextMuted, fontSize = 13.sp,
                )
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(visible, key = { it.packageName }) { game ->
                        GameCard(
                            game = game,
                            onEditLayout = { onEditLayout(game) },
                            onPlay = { if (connected) consentFor = game else onNeedConnection() },
                        )
                    }
                }
            }
        }

        consentFor?.let { game ->
            CaptureConsentModal(
                game = game,
                onCancel = { consentFor = null },
                onStart = { consentFor = null; startSession(game) },
            )
        }
    }
}

@Composable
private fun GameCard(game: LaunchableApp, onEditLayout: () -> Unit, onPlay: () -> Unit) {
    Column(Modifier.fillMaxWidth().nexaCard().padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (game.icon != null) {
                Image(game.icon, null, Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)))
            } else {
                Box(Modifier.size(48.dp).nexaCard(12.dp, background = NexaColors.Border), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.SportsEsports, null, tint = NexaColors.Lavender)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(game.label, color = NexaColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (game.isGame) "Jogo • ${game.packageName}" else "App • ${game.packageName}",
                    color = NexaColors.TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Compatibilidade não verificada",
                    color = NexaColors.Lavender, fontSize = 10.sp,
                    modifier = Modifier.nexaCard(4.dp, background = NexaColors.Obsidian).padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(NexaColors.Border.copy(alpha = 0.7f)))
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.nexaCard(8.dp, background = NexaColors.Obsidian).clickable(onClick = onEditLayout)
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Outlined.Tune, null, tint = NexaColors.VioletGlow, modifier = Modifier.size(14.dp))
                Text("Controle", color = NexaColors.TextMuted, fontSize = 12.sp)
            }
            Row(
                Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(NexaColors.Violet).clickable(onClick = onPlay)
                    .padding(vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, null, tint = NexaColors.Text, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Jogar na TV", color = NexaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun CaptureConsentModal(game: LaunchableApp, onCancel: () -> Unit, onStart: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).clickable(enabled = true, onClick = {}),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth()
                .nexaCard(borderColor = NexaColors.Violet.copy(alpha = 0.6f)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(NexaColors.Border), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.GppMaybe, null, tint = NexaColors.Lavender, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Autorizar captura de tela e áudio?", color = NexaColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Android MediaProjection • NEXA", color = NexaColors.TextMuted, fontSize = 11.sp)
                }
            }
            Text(
                "${game.label} será aberto no celular e a imagem/áudio serão transmitidos para a TV pela rede local. " +
                    "A tela do celular exibirá apenas os controles virtuais.",
                color = NexaColors.TextMuted, fontSize = 12.sp, lineHeight = 17.sp,
            )
            Column(Modifier.fillMaxWidth().nexaCard(10.dp, background = NexaColors.Obsidian).padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Bullet("No diálogo do Android, escolha \"Um único app\" e selecione o jogo (Android 14+). Com \"Tela inteira\" a TV mostrará a tela preta de controles.")
                Bullet("Será pedida a permissão \"Sobrepor a outros apps\" para a tela de controles.")
                Bullet("O áudio só é capturado se o jogo permitir.")
                Bullet("Os controles virtuais ainda não enviam comandos ao jogo (em validação).")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Cancelar", color = NexaColors.TextMuted, fontSize = 13.sp,
                    modifier = Modifier.weight(1f).nexaCard(12.dp, background = NexaColors.Obsidian).clickable(onClick = onCancel)
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Iniciar Agora", color = NexaColors.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(NexaColors.Violet)
                        .clickable(onClick = onStart).padding(vertical = 10.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
// EDITOR DE CONTROLE (Gemini "MOBILE SCREEN 5" em modo Personalizar)
// ----------------------------------------------------------------------------
@Composable
private fun ControllerEditor(target: LaunchableApp?, onClose: () -> Unit) {
    val context = LocalContext.current
    val store = context.nexa.layouts
    var layout by remember(target) { mutableStateOf(store.load(target?.packageName)) }
    var customizing by remember { mutableStateOf(true) }

    // Controller is a landscape surface, like during a session.
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val previous = activity?.requestedOrientation
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { if (previous != null) activity?.requestedOrientation = previous }
    }

    fun close() {
        store.save(target?.packageName, layout)
        onClose()
    }
    BackHandler { close() }

    ControllerPad(
        layout = layout,
        onLayoutChange = { layout = it },
        customizing = customizing,
        onToggleCustomize = {
            if (customizing) store.save(target?.packageName, layout)
            customizing = !customizing
        },
        statusText = "Editor de layout • ${target?.label ?: "Padrão"}",
        footerText = "Modo teste • os botões não enviam comandos",
        endLabel = "Fechar",
        onEnd = { close() },
        onEvent = UnavailableInputBridge::send,
        modifier = Modifier.safeDrawingPadding(),
    )
}

// ----------------------------------------------------------------------------
// CONFIGURAÇÕES (locais)
// ----------------------------------------------------------------------------
@Composable
private fun SettingsScreen(onBack: () -> Unit) {
    val store = LocalContext.current.nexa.settings
    val settings by store.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Header("Configurações", onBack)

        Column(Modifier.fillMaxWidth().nexaCard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Eyebrow("Qualidade da transmissão")
            StreamQuality.entries.forEach { q ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(if (settings.quality == q) NexaColors.Violet.copy(alpha = 0.2f) else Color.Transparent)
                        .border(1.dp, if (settings.quality == q) NexaColors.Violet else NexaColors.Border, RoundedCornerShape(10.dp))
                        .clickable { store.update(settings.copy(quality = q)) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusDot(if (settings.quality == q) NexaColors.VioletGlow else NexaColors.Border)
                    Spacer(Modifier.width(10.dp))
                    Text(q.label, color = NexaColors.Text, fontSize = 13.sp)
                }
            }
        }

        Row(Modifier.fillMaxWidth().nexaCard().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Transmitir áudio do jogo", color = NexaColors.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Usa AudioPlaybackCapture (não grava o microfone).", color = NexaColors.TextMuted, fontSize = 12.sp)
            }
            Switch(
                checked = settings.audioEnabled,
                onCheckedChange = { store.update(NexaSettings(settings.quality, it)) },
                colors = SwitchDefaults.colors(checkedTrackColor = NexaColors.Violet),
            )
        }

        Column(Modifier.fillMaxWidth().nexaCard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Sobre a V1")
                Spacer(Modifier.width(8.dp))
                PlaceholderBadge("Em validação")
            }
            Bullet("Sem login, sem nuvem: tudo fica salvo neste celular.")
            Bullet("Controles virtuais: layout salvo por jogo; envio de comandos ao jogo ainda não disponível.")
            Bullet("Nem todo app é compatível com captura de tela/áudio.")
        }
    }
}

// ----------------------------------------------------------------------------
// Componentes locais (estilo Gemini)
// ----------------------------------------------------------------------------
@Composable
private fun Header(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconSquare(Icons.AutoMirrored.Filled.KeyboardArrowLeft, onBack, size = 32.dp)
        Spacer(Modifier.weight(1f))
        Text(title, color = NexaColors.Text, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun IconSquare(icon: ImageVector, onClick: () -> Unit, size: Dp = 36.dp) {
    Box(
        Modifier.size(size).nexaCard(12.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = NexaColors.Lavender, modifier = Modifier.size(18.dp)) }
}

@Composable
private fun Pill(text: String, active: Boolean) {
    Row(
        Modifier.clip(RoundedCornerShape(50))
            .background(if (active) NexaColors.Violet.copy(alpha = 0.2f) else NexaColors.Obsidian)
            .border(1.dp, if (active) NexaColors.Violet.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusDot(if (active) NexaColors.VioletGlow else NexaColors.TextMuted, size = 6.dp)
        Spacer(Modifier.width(6.dp))
        Text(text, color = if (active) NexaColors.Text else NexaColors.TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun PrimaryButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(NexaColors.Violet).clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = NexaColors.Text, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = NexaColors.Text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SecondaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.nexaCard(12.dp).clickable(onClick = onClick).padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = NexaColors.VioletGlow, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = NexaColors.TextMuted, fontSize = 13.sp)
    }
}

@Composable
private fun SmallAction(text: String, primary: Boolean, onClick: () -> Unit) {
    Text(
        text,
        color = if (primary) NexaColors.Text else NexaColors.TextMuted,
        fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .background(if (primary) NexaColors.Violet else NexaColors.Obsidian)
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        color = if (selected) NexaColors.Text else NexaColors.TextMuted,
        fontSize = 12.sp, fontWeight = FontWeight.Medium,
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .background(if (selected) NexaColors.Violet else NexaColors.Plum)
            .border(1.dp, if (selected) NexaColors.Violet else NexaColors.Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun Bullet(text: String) {
    Row {
        Text("•", color = NexaColors.VioletGlow, fontSize = 12.sp)
        Spacer(Modifier.width(6.dp))
        Text(text, color = NexaColors.TextMuted, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun ViewfinderCorners() {
    val c = NexaColors.VioletGlow
    val len = 20.dp
    val w = 2.dp
    Box(Modifier.fillMaxSize()) {
        // top-left
        Box(Modifier.align(Alignment.TopStart).size(len, w).background(c))
        Box(Modifier.align(Alignment.TopStart).size(w, len).background(c))
        // top-right
        Box(Modifier.align(Alignment.TopEnd).size(len, w).background(c))
        Box(Modifier.align(Alignment.TopEnd).size(w, len).background(c))
        // bottom-left
        Box(Modifier.align(Alignment.BottomStart).size(len, w).background(c))
        Box(Modifier.align(Alignment.BottomStart).size(w, len).background(c))
        // bottom-right
        Box(Modifier.align(Alignment.BottomEnd).size(len, w).background(c))
        Box(Modifier.align(Alignment.BottomEnd).size(w, len).background(c))
    }
}
