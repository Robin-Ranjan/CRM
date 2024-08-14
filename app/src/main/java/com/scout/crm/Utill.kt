package com.scout.crm

import android.app.Activity
import android.os.Build
import android.view.WindowManager

object Utill {
    fun statusBarColor(color: Int, act: Activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            val window = act.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = act.resources.getColor(color)
        }
    }
}