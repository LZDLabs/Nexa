package com.nexa.mobile.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.nexa.mobile.games.InstalledApps
import com.nexa.mobile.games.LaunchableApp
import com.nexa.mobile.nexa
import com.nexa.mobile.stream.StreamingService

/**
 * Explicit, step-by-step consent before a session:
 * 1. "Display over other apps" (black controller overlay)
 * 2. RECORD_AUDIO (game audio capture) + notifications — optional, the session runs without them
 * 3. Android's own screen-capture dialog (MediaProjection). On Android 14 QPR2+ the user can pick
 *    a single app here, which is what keeps the black overlay out of the TV picture.
 */
@Composable
fun rememberSessionStarter(): (LaunchableApp) -> Unit {
    val context = LocalContext.current
    var pending by remember { mutableStateOf<LaunchableApp?>(null) }

    val projectionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val app = pending
        pending = null
        val data = result.data
        if (app == null || result.resultCode != Activity.RESULT_OK || data == null) {
            Toast.makeText(context, "Captura não autorizada. A sessão não foi iniciada.", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        StreamingService.start(context, result.resultCode, data, app.packageName, app.label)
        if (!InstalledApps.launch(context, app.packageName)) {
            Toast.makeText(context, "Não foi possível abrir ${app.label}.", Toast.LENGTH_SHORT).show()
        }
    }

    fun requestProjection() {
        val mpm = context.getSystemService(MediaProjectionManager::class.java)
        projectionLauncher.launch(mpm.createScreenCaptureIntent())
    }

    val permissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants[Manifest.permission.RECORD_AUDIO] == false) {
            Toast.makeText(context, "Sem permissão de áudio: a TV receberá só o vídeo.", Toast.LENGTH_SHORT).show()
        }
        requestProjection()
    }

    return { app ->
        if (!Settings.canDrawOverlays(context)) {
            Toast.makeText(
                context,
                "Ative \"Sobrepor a outros apps\" para o NEXA e toque em Iniciar novamente.",
                Toast.LENGTH_LONG,
            ).show()
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            pending = app
            val needed = buildList {
                if (context.nexa.settings.state.value.audioEnabled && !granted(context, Manifest.permission.RECORD_AUDIO)) {
                    add(Manifest.permission.RECORD_AUDIO)
                }
                if (Build.VERSION.SDK_INT >= 33 && !granted(context, Manifest.permission.POST_NOTIFICATIONS)) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            if (needed.isEmpty()) requestProjection() else permissionsLauncher.launch(needed.toTypedArray())
        }
    }
}

private fun granted(context: Context, permission: String) =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
