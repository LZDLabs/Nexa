package com.nexa.mobile.games

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class LaunchableApp(
    val packageName: String,
    val label: String,
    /** True when the developer declared the app as a game (category/flag). Not a compatibility guarantee. */
    val isGame: Boolean,
    val icon: ImageBitmap?,
)

/**
 * Lists apps with a launcher entry. Visibility on Android 11+ comes from the
 * `<queries>` LAUNCHER intent in the manifest — NEXA does not use QUERY_ALL_PACKAGES.
 */
object InstalledApps {

    fun load(context: Context): List<LaunchableApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val infos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }
        return infos.asSequence()
            .map { it.activityInfo }
            .filter { it.packageName != context.packageName }
            .distinctBy { it.packageName }
            .map { ai ->
                val app = ai.applicationInfo
                LaunchableApp(
                    packageName = ai.packageName,
                    label = ai.loadLabel(pm).toString(),
                    isGame = isGame(app),
                    icon = runCatching { ai.loadIcon(pm).toBitmap(96, 96).asImageBitmap() }.getOrNull(),
                )
            }
            .sortedWith(compareByDescending<LaunchableApp> { it.isGame }.thenBy { it.label.lowercase() })
            .toList()
    }

    @Suppress("DEPRECATION")
    private fun isGame(app: ApplicationInfo) =
        app.category == ApplicationInfo.CATEGORY_GAME || (app.flags and ApplicationInfo.FLAG_IS_GAME) != 0

    fun launch(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent) }.isSuccess
    }
}
