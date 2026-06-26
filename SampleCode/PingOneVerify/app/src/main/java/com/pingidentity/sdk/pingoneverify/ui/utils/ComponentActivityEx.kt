package com.pingidentity.sdk.pingoneverify.ui.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.pingidentity.sdk.pingoneverify.sample.R

object ComponentFragment {
    @JvmStatic
    fun Fragment.applyWindowsInsetListener() {
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()
            )
            v.updatePadding(
                top = bars.top,
                bottom = bars.bottom
            )
            if (this is DialogFragment) {
                dialog?.window?.let {
                    WindowCompat.getInsetsController(it, it.decorView).isAppearanceLightNavigationBars = true
                }
            }
            insets
        }
    }
}

object ComponentActivityEx {
    const val STATUS_BAR_TAG = "status_bar"

    @JvmStatic
    fun ComponentActivity.applyEdgeToEdgeInsets() {
        val statusBarDrawable = ContextCompat.getDrawable(this, R.color.white)
        // Add this condition if you only want to support edge to edge in Android 15+ devices else remove this.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUR_DEVELOPMENT) {
            val view = findViewById<View>(android.R.id.content)
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
                val bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout() or WindowInsetsCompat.Type.ime()
                )

                val statusBarHeight =
                    windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top

                applyStatusBarColor(window, statusBarDrawable, false, statusBarHeight)
                WindowCompat.getInsetsController(
                    window,
                    window.decorView
                ).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(
                    window,
                    window.decorView
                ).isAppearanceLightNavigationBars = true
                v.updatePadding(
                    left = bars.left,
                    top = bars.top,
                    right = bars.right,
                    bottom = bars.bottom,
                )
                windowInsets
            }
        }
    }

    fun applyStatusBarColor(
        window: Window,
        statusBarBackground: Drawable?,
        isDecor: Boolean,
        height: Int
    ): View {
        val parent =
            if (isDecor) window.decorView as ViewGroup else (window.findViewById<View>(android.R.id.content) as ViewGroup)
        var fakeStatusBarView = parent.findViewWithTag<View>(STATUS_BAR_TAG)
        if (fakeStatusBarView != null) {
            if (fakeStatusBarView.visibility == View.GONE) {
                fakeStatusBarView.visibility = View.VISIBLE
            }
            fakeStatusBarView.background = statusBarBackground
        } else {
            fakeStatusBarView = createStatusBarView(window.context, statusBarBackground, height)
            parent.addView(fakeStatusBarView)
        }
        return fakeStatusBarView
    }

    private fun createStatusBarView(
        context: Context,
        statusBarBackground: Drawable?,
        height: Int
    ): View {
        val statusBarView = View(context)
        statusBarView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, height
        )
        statusBarView.background = statusBarBackground
        statusBarView.tag = STATUS_BAR_TAG
        return statusBarView
    }
}