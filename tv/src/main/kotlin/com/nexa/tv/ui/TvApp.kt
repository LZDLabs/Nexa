package com.nexa.tv.ui

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.nexa.tv.Phase
import com.nexa.tv.QrCode
import com.nexa.tv.TvUiState
import com.nexa.tv.TvViewModel
import com.nexa.ui.Eyebrow
import com.nexa.ui.LogoSize
import com.nexa.ui.NexaColors
import com.nexa.ui.NexaLogo
import com.nexa.ui.StatusPill
import com.nexa.ui.nexaCard
import kotlinx.coroutines.delay

@Composable
fun TvApp(vm: TvViewModel) {
    val state by vm.state.collectAsState()
    var showPairing by rememberSaveable { mutableStateOf(false) }

    // Keep the TV awake only while a game is on screen.
    val view = LocalView.current
    LaunchedEffect(state.phase) { view.keepScreenOn = state.phase == Phase.STREAMING }
    LaunchedEffect(state.phase) { if (state.phase == Phase.WAITING) showPairing = false }

    when {
        state.phase == Phase.STREAMING -> StreamingScreen(state, vm)
        state.phase == Phase.CONNECTED || showPairing -> PairingScreen(
            state,
            onBack = { showPairing = false },
            onDisconnect = vm::disconnect,
        )
        else -> HomeScreen(state, onStartConnection = { showPairing = true }, onNewCode = vm::newCode)
    }
}

/** Gemini ambient glow: radial violet light from the top (non-streaming screens only). */
private val AmbientGlow = Brush.radialGradient(
    colors = listOf(NexaColors.Violet.copy(alpha = 0.16f), Color.Transparent),
    center = androidx.compose.ui.geometry.Offset(960f, 0f),
    radius = 1100f,
)

// ----------------------------------------------------------------------------
// TV SCREEN 1: TELA INICIAL (QR Code + código alternativo)
// ----------------------------------------------------------------------------
@Composable
private fun HomeScreen(state: TvUiState, onStartConnection: () -> Unit, onNewCode: () -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Column(
        Modifier.fillMaxSize().background(NexaColors.Obsidian).background(AmbientGlow).padding(horizontal = 48.dp, vertical = 32.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NexaLogo(LogoSize.Small)
            Spacer(Modifier.weight(1f))
            StatusPill(
                if (state.host != null) "Rede local • Pronto" else "Sem rede local",
                active = state.host != null,
            )
        }

        Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Left: welcome + D-pad actions
            Column(Modifier.weight(7f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Eyebrow(
                    "Receptor Android TV",
                    modifier = Modifier.nexaCard(6.dp, NexaColors.Violet.copy(alpha = 0.3f)).padding(horizontal = 10.dp, vertical = 4.dp),
                )
                Text("Pronto para jogar na tela grande.", color = NexaColors.Text, fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
                Text(
                    "Conecte seu smartphone Android na mesma rede Wi-Fi para transmitir seus jogos.",
                    color = NexaColors.TextMuted, fontSize = 15.sp, lineHeight = 22.sp, modifier = Modifier.widthIn(max = 460.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TvButton("Iniciar Conexão", Icons.Outlined.Sensors, primary = true, onClick = onStartConnection, modifier = Modifier.focusRequester(focus))
                    TvButton("Novo código", Icons.Outlined.Refresh, primary = false, onClick = onNewCode)
                }
                Row(Modifier.widthIn(max = 520.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1. Abra o NEXA no celular", "2. Escaneie o QR Code ao lado", "3. Escolha o jogo e jogue").forEach {
                        Text(
                            it, color = NexaColors.TextMuted, fontSize = 12.sp, lineHeight = 16.sp,
                            modifier = Modifier.weight(1f).nexaCard(10.dp, background = NexaColors.Plum.copy(alpha = 0.75f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            Spacer(Modifier.width(32.dp))
            // Right: QR + alternative code
            Column(
                Modifier.weight(5f).nexaCard(18.dp, background = NexaColors.Plum.copy(alpha = 0.9f)).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.qrPayload != null) {
                    NexaQr(state.qrPayload)
                } else {
                    Box(Modifier.size(196.dp).nexaCard(14.dp, background = NexaColors.Obsidian), contentAlignment = Alignment.Center) {
                        Text("Conecte a TV ao Wi-Fi\npara exibir o QR Code", color = NexaColors.TextMuted, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Eyebrow("Código Alternativo", color = NexaColors.TextMuted, fontSize = 10)
                Spacer(Modifier.height(6.dp))
                Text(
                    state.code,
                    color = NexaColors.Text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,
                    fontSize = 22.sp, letterSpacing = 5.sp,
                    modifier = Modifier.nexaCard(10.dp, NexaColors.Violet.copy(alpha = 0.4f), NexaColors.Obsidian)
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(8.dp))
                ExpiryText(state.expiresAtMs)
            }
        }

        Row(Modifier.fillMaxWidth()) {
            Text("Use o controle remoto (D-Pad) para navegar", color = NexaColors.TextMuted, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(
                state.host?.let { "IP Local: $it:${state.port}" } ?: "Sem IP local",
                color = NexaColors.Lavender, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun NexaQr(payload: String) {
    val bitmap = remember(payload) {
        QrCode.render(payload, 512, NexaColors.Obsidian.toArgb(), NexaColors.Text.toArgb())
    }
    Box(
        Modifier
            .shadow(28.dp, RoundedCornerShape(14.dp), ambientColor = NexaColors.Violet, spotColor = NexaColors.Violet)
            .clip(RoundedCornerShape(14.dp))
            .background(NexaColors.Text)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(bitmap, contentDescription = "QR Code de pareamento", modifier = Modifier.size(196.dp))
        // Center NEXA emblem (error correction H keeps the code readable)
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(NexaColors.Plum)
                .border(2.dp, NexaColors.Violet, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) { Text("NX", color = NexaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Black) }
    }
}

@Composable
private fun ExpiryText(expiresAtMs: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(expiresAtMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }
    val left = ((expiresAtMs - now) / 1000).coerceAtLeast(0)
    Text(
        "Código temporário • expira em %d:%02d".format(left / 60, left % 60),
        color = NexaColors.TextMuted, fontSize = 11.sp,
    )
}

// ----------------------------------------------------------------------------
// TV SCREEN 2: CONEXÃO / ESPERA
// ----------------------------------------------------------------------------
@Composable
private fun PairingScreen(state: TvUiState, onBack: () -> Unit, onDisconnect: () -> Unit) {
    val paired = state.phase == Phase.CONNECTED
    val focus = remember { FocusRequester() }
    LaunchedEffect(paired) { runCatching { focus.requestFocus() } }
    BackHandler(enabled = !paired, onBack = onBack)

    Column(
        Modifier.fillMaxSize().background(NexaColors.Obsidian).background(AmbientGlow).padding(horizontal = 48.dp, vertical = 32.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NexaLogo(LogoSize.Small)
            Spacer(Modifier.weight(1f))
            StatusPill(
                if (paired) "Conectado via Wi-Fi Local" else "Aguardando pareamento do celular...",
                active = paired, pulsing = !paired,
            )
        }

        Column(Modifier.weight(1f).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Radar()
            Spacer(Modifier.height(20.dp))
            if (paired) {
                Eyebrow("Dispositivo Conectado", fontSize = 13)
                Text(state.phoneName.orEmpty(), color = NexaColors.Text, fontSize = 30.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                Text(
                    "Selecione um jogo em Meus Jogos no seu celular para iniciar a transmissão.",
                    color = NexaColors.TextMuted, fontSize = 15.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp).widthIn(max = 520.dp),
                )
            } else {
                Eyebrow("Procurando Celular", color = NexaColors.TextMuted, fontSize = 13)
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Código de Sessão: ", color = NexaColors.Text, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(state.code, color = NexaColors.Lavender, fontSize = 26.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Text(
                    "Escaneie o QR Code ou digite o código no aplicativo NEXA Mobile.",
                    color = NexaColors.TextMuted, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp),
                )
            }
            Spacer(Modifier.height(28.dp))
            if (paired) {
                TvButton("Desconectar", Icons.Outlined.PowerSettingsNew, primary = false, onClick = onDisconnect, modifier = Modifier.focusRequester(focus))
            } else {
                TvButton("Mostrar QR Code", Icons.Outlined.Sensors, primary = true, onClick = onBack, modifier = Modifier.focusRequester(focus))
            }
        }

        Row(Modifier.fillMaxWidth()) {
            Text("Vídeo H.264 • Áudio PCM estéreo • Rede local", color = NexaColors.TextMuted, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(state.tvName, color = NexaColors.TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun Radar() {
    val t = rememberInfiniteTransition(label = "radar")
    val ping by t.animateFloat(0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "ping")
    Box(Modifier.size(140.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.fillMaxSize().scale(0.6f + ping * 0.4f).alpha(1f - ping)
                .border(1.dp, NexaColors.Violet.copy(alpha = 0.5f), CircleShape)
        )
        Box(Modifier.size(110.dp).border(1.dp, NexaColors.VioletGlow.copy(alpha = 0.45f), CircleShape))
        Box(
            Modifier.size(80.dp)
                .shadow(24.dp, RoundedCornerShape(18.dp), ambientColor = NexaColors.Violet, spotColor = NexaColors.Violet)
                .clip(RoundedCornerShape(18.dp)).background(NexaColors.Plum)
                .border(2.dp, NexaColors.Violet, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) { Icon(Icons.Outlined.Smartphone, null, tint = NexaColors.Text, modifier = Modifier.size(40.dp)) }
    }
}

// ----------------------------------------------------------------------------
// TV SCREEN 3: TRANSMISSÃO (vídeo real do celular em tela cheia)
// ----------------------------------------------------------------------------
@Composable
private fun StreamingScreen(state: TvUiState, vm: TvViewModel) {
    val endFocus = remember { FocusRequester() }
    var menuVisible by remember { mutableStateOf(false) }
    // Back on the remote reveals and focuses "Encerrar Transmissão"; Back again hides it.
    BackHandler { menuVisible = !menuVisible }
    LaunchedEffect(menuVisible) { if (menuVisible) runCatching { endFocus.requestFocus() } }
    // Hide the overlay again after a while.
    LaunchedEffect(menuVisible) { if (menuVisible) { delay(8_000); menuVisible = false } }

    Box(Modifier.fillMaxSize().background(NexaColors.PureBlack), contentAlignment = Alignment.Center) {
        val video = state.video
        if (video != null && video.width > 0 && video.height > 0) {
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).apply {
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(holder: SurfaceHolder) = vm.attachSurface(holder.surface)
                            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
                            override fun surfaceDestroyed(holder: SurfaceHolder) = vm.attachSurface(null)
                        })
                    }
                },
                modifier = Modifier.aspectRatio(video.width.toFloat() / video.height),
            )
        }

        // Minimal top-right connection pill
        Row(
            Modifier.align(Alignment.TopEnd).padding(16.dp)
                .clip(CircleShape).background(NexaColors.Obsidian.copy(alpha = 0.8f))
                .border(1.dp, NexaColors.Violet.copy(alpha = 0.4f), CircleShape)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(NexaColors.VioletGlow))
            Spacer(Modifier.width(8.dp))
            Text(
                listOfNotNull("NEXA", state.appLabel, video?.let { "${it.width}×${it.height}" }).joinToString(" • "),
                color = NexaColors.Text, fontSize = 12.sp, fontWeight = FontWeight.Medium,
            )
        }

        if (state.paused) {
            Column(
                Modifier.fillMaxSize().background(NexaColors.PureBlack.copy(alpha = 0.7f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Eyebrow(
                    "Sessão Pausada",
                    modifier = Modifier.nexaCard(6.dp, NexaColors.Violet.copy(alpha = 0.4f)).padding(horizontal = 12.dp, vertical = 4.dp),
                )
                Spacer(Modifier.height(8.dp))
                Text("Transmissão pausada pelo celular", color = NexaColors.Text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(Modifier.align(Alignment.BottomEnd).padding(16.dp).alpha(if (menuVisible) 1f else 0.75f)) {
            TvButton(
                "Encerrar Transmissão", Icons.Outlined.PowerSettingsNew, primary = false,
                onClick = vm::endSession, modifier = Modifier.focusRequester(endFocus), compact = true,
            )
        }
    }
}

// ----------------------------------------------------------------------------
// D-Pad focusable button (Gemini focus ring: violet + ring-4 + scale 1.02)
// ----------------------------------------------------------------------------
@Composable
private fun TvButton(
    text: String,
    icon: ImageVector,
    primary: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(12.dp)
    val bg = when {
        focused && primary -> NexaColors.Violet
        focused -> NexaColors.Border
        primary -> NexaColors.Plum
        else -> NexaColors.Plum.copy(alpha = 0.8f)
    }
    Row(
        modifier
            .scale(if (focused) 1.04f else 1f)
            .then(if (focused) Modifier.border(4.dp, NexaColors.VioletGlow.copy(alpha = 0.45f), RoundedCornerShape(16.dp)).padding(4.dp) else Modifier.padding(4.dp))
            .clip(shape)
            .background(bg)
            .border(1.dp, if (focused) Color.Transparent else NexaColors.Border, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = if (compact) 14.dp else 20.dp, vertical = if (compact) 8.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, null, tint = if (focused) NexaColors.Text else NexaColors.Lavender, modifier = Modifier.size(if (compact) 16.dp else 18.dp))
        Text(text, color = NexaColors.Text, fontSize = if (compact) 13.sp else 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
