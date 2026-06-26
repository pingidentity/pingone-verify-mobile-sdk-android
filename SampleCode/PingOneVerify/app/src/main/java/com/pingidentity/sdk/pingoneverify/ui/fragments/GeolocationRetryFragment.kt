package com.pingidentity.sdk.pingoneverify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass
import com.pingidentity.sdk.pingoneverify.sample.databinding.FragmentGeolocationRetryBinding
import com.pingidentity.sdk.pingoneverify.sample.R
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract

/**
 * Shown when device location is unavailable or permission is denied.
 * Retry calls captureGeolocation() — UI-only, no backend call.
 * The 1-second processing delay before showing this screen is owned by DocumentCapturePresenter.
 * Mirrors iOS GeolocationPermissionRetryViewController.
 */
class GeolocationRetryFragment(
    coordinator: VerifyTransactionCoordinator?,
) : BaseFragment(coordinator), View.OnClickListener {

    private lateinit var binding: FragmentGeolocationRetryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setDocumentClass(DocumentClass.GEOLOCATION)
        binding = FragmentGeolocationRetryBinding.inflate(inflater, container, false).apply {
            theme = mAppTheme
            languageProvider = mLanguageProvider
            btnRetry.setOnClickListener(this@GeolocationRetryFragment)
            btnCancel.setOnClickListener(this@GeolocationRetryFragment)
        }
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_retry -> retryCapture()
            R.id.btn_cancel -> onCancel(v)
        }
    }

    private fun retryCapture() {
        mCoordinator?.captureGeolocation(requireContext())
    }

    companion object {
        @JvmStatic
        fun newInstance(
            appTheme: AppThemeResponse?,
            coordinator: VerifyTransactionCoordinator?,
            languageProvider: LanguagePackProviderContract?,
        ): GeolocationRetryFragment {
            val fragment = GeolocationRetryFragment(coordinator)
            fragment.mAppTheme = appTheme
            fragment.mLanguageProvider = languageProvider
            return fragment
        }
    }
}
