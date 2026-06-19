package com.cibertec.adoptapet.util

import android.app.Activity
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.cibertec.adoptapet.R

object SystemBarUtils {
    fun aplicarBarras(activity: Activity) {
        activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.color_principal_oscuro)
        activity.window.navigationBarColor = ContextCompat.getColor(activity, R.color.color_principal_oscuro)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowInsetsControllerCompat(activity.window, activity.window.decorView)
                .isAppearanceLightNavigationBars = false
        }
    }
}
