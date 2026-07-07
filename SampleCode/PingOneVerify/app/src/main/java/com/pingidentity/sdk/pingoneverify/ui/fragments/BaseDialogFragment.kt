package com.pingidentity.sdk.pingoneverify.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.DialogFragment
import com.pingidentity.sdk.pingoneverify.analytics.AppEventManagerImpl
import com.pingidentity.sdk.pingoneverify.analytics.constants.AppEventError
import com.pingidentity.sdk.pingoneverify.neo.listeners.BooleanCallback
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse
import com.pingidentity.sdk.pingoneverify.sample.R
import com.pingidentity.sdk.pingoneverify.ui.utils.ComponentFragment.applyWindowsInsetListener
import com.pingidentity.sdk.pingoneverify.ui.utils.DateFormatter
import com.pingidentity.sdk.pingoneverify.ui.utils.UiUtil
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract

open class BaseDialogFragment(
    protected val appTheme: AppThemeResponse? = null,
    protected val mLanguageProvider: LanguagePackProviderContract,
) : DialogFragment() {
    protected var isClickEnabled = true
    private lateinit var backPressCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBackPressedCallback()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        applyWindowsInsetListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            AppEventManagerImpl.getInstance().flushAppEvents()
        } catch (error: AppEventError) {
            Log.e(TAG, "Failed to submit events", error)
        }
        backPressCallback.remove()
    }

    private fun setBackPressedCallback() {
        backPressCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }.apply {
            requireActivity().onBackPressedDispatcher.addCallback(this@BaseDialogFragment, this)
        }
    }

    protected open fun onBackPressed() {
    }

    protected fun skipClickEvent(): Boolean {
        val clickEnabled: Boolean = isClickEnabled
        if (clickEnabled) {
            UiUtil.clickDelayHandler(requireView(), object : BooleanCallback {
                override fun onResult(result: Boolean) {
                    isClickEnabled = result
                }
            })
        }
        return !clickEnabled
    }

    protected fun getTimeRemainingString(time: Int?, label: Int?): String {
        if (time == null || label == null) return ""
        val minLabel = DateFormatter.getMinutes(requireContext(), mLanguageProvider, time, R.string.sdk_time_min)
        val secLabel = DateFormatter.getSeconds(requireContext(), mLanguageProvider, time, R.string.sdk_time_sec)
        return DateFormatter.getTimeDifferenceFormatted(requireContext(), mLanguageProvider, minLabel, secLabel, label)
    }

    companion object {
        @JvmStatic val TAG: String = BaseDialogFragment::class.java.simpleName
    }
}