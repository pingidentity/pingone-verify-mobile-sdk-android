package com.pingidentity.sdk.pingoneverify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator
import com.pingidentity.sdk.pingoneverify.neo.listeners.PermissionListener
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass
import com.pingidentity.sdk.pingoneverify.neo.models.RetryFeedback
import com.pingidentity.sdk.pingoneverify.neo.settings.DocumentCaptureSettings
import com.pingidentity.sdk.pingoneverify.sample.R
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogUploadFailureBinding
import com.pingidentity.sdk.pingoneverify.ui.UiConstants
import com.pingidentity.sdk.pingoneverify.utils.VerifySessionTimer
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract
import java.util.function.Consumer

class UploadFailureDialog(
    private val onRetryDecision: Consumer<Boolean>,
    private val message: RetryFeedback,
    private val settings: DocumentCaptureSettings,
    coordinator: VerifyTransactionCoordinator?,
) : BaseFragment(coordinator), View.OnClickListener {
    private lateinit var binding: DialogUploadFailureBinding
    private var retryable: Boolean = false

    private val documentClass: DocumentClass
        get() = DocumentClass.permissiveValueOf(settings.documentType?.toString())


    private val mTimerObserver = object : VerifySessionTimer.Timer.TimerObserver {
        override fun onTick(millisRemaining: Int) {
            binding?.txtTimeRemaining?.text = getTimeRemainingString(millisRemaining, R.string.idv_timer_label)
        }
        override fun onFinish() {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        VerifySessionTimer.getInstance().getSessionTimer().removeObserver(mTimerObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        retryable = when (documentClass) {
            DocumentClass.GEOLOCATION -> true
            else -> (settings.getRemainingAttempts() ?: 0) > 0
        }
        binding = DialogUploadFailureBinding.inflate(inflater, container, false).apply {
            theme = mAppTheme
            languageProvider = mLanguageProvider
            isRetryable = retryable
        }
        binding.btnRetry.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        VerifySessionTimer.getInstance().getSessionTimer().addObserver(mTimerObserver)
        var infoTitle: String? = null
        when (documentClass) {
            DocumentClass.GEOLOCATION -> {
                binding.ivInfoImage.setImageResource(R.drawable.idv_upload_error)
                binding.ivInfoImage.setColorFilter(requireContext().getColor(R.color.permission_error))
                infoTitle = getLanguageString(R.string.idv_permission_location_title)
                binding.tvFailMessage.text = getLanguageString(R.string.idv_permission_dialog_message_location)
                binding.btnRetry.text = getLanguageString(R.string.idv_locationCapture_retry)
            }
            else -> {
                val key = message.languagePackKey
                binding.tvFailMessage.text = when {
                    !key.isNullOrEmpty() ->
                        mLanguageProvider?.getStringForResource(key) ?: message.message ?: ""
                    else -> message.message ?: ""
                }
            }
        }
        if (infoTitle == null) {
            infoTitle = getLanguageString(if (retryable) R.string.idv_data_retry else R.string.idv_data_fail)
            binding.ivInfoImage.setImageResource(if (retryable) R.drawable.idv_upload_retry else R.drawable.idv_upload_error)
            binding.btnRetry.text = getLanguageString(if (retryable) R.string.idv_data_retry else R.string.idv_dataCapture_button)
        }
        binding.tvFailTitle.text = infoTitle
        binding.ivInfoImage.contentDescription = infoTitle
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_retry -> {
                if (skipClickEvent()) return
                when (documentClass) {
                    DocumentClass.GEOLOCATION -> {
                        checkPermission(object : PermissionListener {
                            override fun onPermissionGranted() { onRetryDecision.accept(true) }
                            override fun onPermissionDenied() { onRetryDecision.accept(false) }
                        }, PermissionType.LOCATION)
                    }
                    else -> onRetryDecision.accept(retryable)
                }
            }
            R.id.btn_cancel -> onCancel(v)
        }
    }

    companion object {
        @JvmStatic
        val TAG: String = UiConstants.Tags.UPLOAD_FAILURE_DIALOG

        @JvmStatic
        fun newInstance(
            appTheme: AppThemeResponse,
            onRetryDecision: Consumer<Boolean>,
            message: RetryFeedback,
            settings: DocumentCaptureSettings,
            coordinator: VerifyTransactionCoordinator?,
            languagePresenter: LanguagePackProviderContract,
        ): UploadFailureDialog {
            return UploadFailureDialog(onRetryDecision, message, settings, coordinator).apply {
                mAppTheme = appTheme
                mLanguageProvider = languagePresenter
            }
        }
    }
}
