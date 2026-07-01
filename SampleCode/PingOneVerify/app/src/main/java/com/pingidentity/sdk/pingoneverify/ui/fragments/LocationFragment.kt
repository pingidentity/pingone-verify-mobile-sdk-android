package com.pingidentity.sdk.pingoneverify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pingidentity.sdk.pingoneverify.neo.contracts.VerifyTransactionCoordinator
import com.pingidentity.sdk.pingoneverify.neo.listeners.PermissionListener
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass
import com.pingidentity.sdk.pingoneverify.sample.R
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogLocationBinding
import com.pingidentity.sdk.pingoneverify.neo.settings.LocationCaptureSettings
import com.pingidentity.sdk.pingoneverify.utils.VerifySessionTimer
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract

/**
 * UI fragment for the geolocation capture step.
 * Handles permission prompt and button actions — all results flow directly
 * through [coordinator]. No listener crossing into the UI layer.
 */
class LocationFragment(
    private var captureSettings: LocationCaptureSettings,
    coordinator: VerifyTransactionCoordinator?,
    private val onPermissionDenied: Runnable? = null,
) : BaseFragment(coordinator), View.OnClickListener {

    private lateinit var binding: DialogLocationBinding


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
        setDocumentClass(DocumentClass.GEOLOCATION)
        binding = DialogLocationBinding.inflate(inflater, container, false).apply {
            theme = mAppTheme
            languageProvider = mLanguageProvider
            btnCapture.setOnClickListener(this@LocationFragment)
            btnCancel.setOnClickListener(this@LocationFragment)
            btnSkip.setOnClickListener(this@LocationFragment)
            btnSkip.visibility = if (captureSettings.isOptional) View.VISIBLE else View.GONE
            VerifySessionTimer.getInstance().getSessionTimer().addObserver(mTimerObserver)
        }
        return binding.root
    }

    private fun requestLocation() {
        checkPermission(object : PermissionListener {
            override fun onPermissionGranted() {
                mCoordinator?.captureGeolocation(requireContext())
            }

            override fun onPermissionDenied() {
                onPermissionDenied?.run() ?: mCoordinator?.endVerification()
            }
        }, PermissionType.LOCATION, true)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_cancel -> onCancel(v)
            R.id.btn_capture -> requestLocation()
            R.id.btn_skip -> mCoordinator?.skipDocument(DocumentClass.GEOLOCATION)
        }
    }

    companion object {
        @JvmStatic
        val TAG: String = LocationFragment::class.java.name

        @JvmStatic
        fun newInstance(
            appTheme: AppThemeResponse?,
            settings: LocationCaptureSettings,
            coordinator: VerifyTransactionCoordinator?,
            languagePresenter: LanguagePackProviderContract?,
            onPermissionDenied: Runnable? = null,
        ): LocationFragment {
            val locationFragment = LocationFragment(settings, coordinator, onPermissionDenied)
            locationFragment.mAppTheme = appTheme
            locationFragment.mLanguageProvider = languagePresenter
            return locationFragment
        }
    }
}
