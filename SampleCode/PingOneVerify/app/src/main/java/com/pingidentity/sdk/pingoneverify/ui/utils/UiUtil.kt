package com.pingidentity.sdk.pingoneverify.ui.utils

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Size
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import com.pingidentity.sdk.pingoneverify.models.Constants
import com.pingidentity.sdk.pingoneverify.neo.listeners.BooleanCallback

object UiUtil {

    @JvmStatic fun hideKeyboard(editText: EditText) {
        hideKeyboard(editText, false)
    }

    @JvmStatic fun hideKeyboard(editText: EditText, isClear: Boolean) {
        val inputMethodManager = editText.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
        if (isClear) editText.text.clear()
    }

    @JvmStatic fun getScreenSize(context: Context): Size {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.apply { getMetrics(displayMetrics) }
        return Size(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    @JvmStatic fun updateFragmentAccessibility(fragment: Fragment, isVisible: Boolean) {
        fragment.view?.setImportantForAccessibility(
            if (isVisible) View.IMPORTANT_FOR_ACCESSIBILITY_YES
            else View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
        )
    }

    @JvmStatic fun clickDelayHandler(view: View, callback: BooleanCallback) {
        callback.onResult(false)
        view.postDelayed({ callback.onResult(true) }, Constants.DEFAULT_CLICK_DELAY.toLong())
    }

    @JvmStatic fun preventRepeatedClicksFor(timeInMillis: Int, @NonNull view: View) {
        view.post {
            view.isEnabled = false
            view.postDelayed({ view.isEnabled = true }, timeInMillis.toLong())
        }
    }
}
