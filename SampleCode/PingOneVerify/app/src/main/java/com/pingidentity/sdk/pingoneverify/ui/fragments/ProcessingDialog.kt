package com.pingidentity.sdk.pingoneverify.ui.fragments

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogProcessingBinding
import com.pingidentity.sdk.pingoneverify.ui.UiConstants
import com.pingidentity.sdk.pingoneverify.utils.PollingHelper
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract
import java.util.concurrent.TimeUnit

class ProcessingDialog : BaseFragment(null) {
    lateinit var binding: DialogProcessingBinding
    lateinit var views: List<View>
    private var timer: CountDownTimer? = null
    private var currentIndex = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogProcessingBinding.inflate(inflater, container, false).apply {
            theme = mAppTheme
            languageProvider = mLanguageProvider
        }
        views = listOf(binding.progress1, binding.progress2, binding.progress3)
        requireContext().resources
            .getIdentifier("idv_progress_color", "color", requireContext().packageName)
            .takeIf { it != 0 }?.let { colorId ->
                for (view in views) {
                    (view.background as GradientDrawable).setColor(colorId)
                }
            }
        return binding.root
    }

    private fun updateProcessing() {
        for (index in views.indices) {
            views[(currentIndex + index) % views.size].alpha = loaderAlpha[index]
        }
    }

    private fun setupTimer() {
        timer = object : CountDownTimer(TimeUnit.HOURS.toMillis(1), 500) {
            override fun onTick(millisUntilFinished: Long) {
                currentIndex++
                updateProcessing()
            }

            override fun onFinish() {
                //setupTimer();
            }
        }.apply { start() }
    }

    override fun onStart() {
        super.onStart()
        setupTimer()
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
        timer = null
    }

    override fun pollForMessages(start: Boolean) {
        if (start) {
            PollingHelper.getInstance().startPolling()
        } else {
            PollingHelper.getInstance().stopPolling()
        }
    }

    companion object {
        @JvmStatic
        val TAG: String = UiConstants.Tags.PROCESSING_DIALOG
        val loaderAlpha = floatArrayOf(0.2f, 0.5f, 1f)

        @JvmStatic
        fun newInstance(
            appTheme: AppThemeResponse,
            languagePresenter: LanguagePackProviderContract,
        ): ProcessingDialog {
            return ProcessingDialog().apply {
                mAppTheme = appTheme
                mLanguageProvider = languagePresenter
            }
        }
    }
}
