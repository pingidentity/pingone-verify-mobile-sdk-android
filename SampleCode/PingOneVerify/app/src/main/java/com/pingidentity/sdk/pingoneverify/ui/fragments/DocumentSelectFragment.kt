package com.pingidentity.sdk.pingoneverify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass
import com.pingidentity.sdk.pingoneverify.sample.R
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogDocumentSelectBinding
import com.pingidentity.sdk.pingoneverify.ui.views.DocumentAdapter
import com.pingidentity.sdk.pingoneverify.ui.views.DocumentCallback
import com.pingidentity.sdk.pingoneverify.utils.DocumentSubmissionTimer
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract

class DocumentSelectFragment(
    theme: AppThemeResponse?,
    languagePresenter: LanguagePackProviderContract,
    private val documentCallback: DocumentCallback
) : BaseDialogFragment(theme, languagePresenter), DocumentCallback {
    private lateinit var binding: DialogDocumentSelectBinding


    private val mTimerObserver = object : DocumentSubmissionTimer.Observer {
        override fun onTick(millisRemaining: Int) {
            binding?.txtTimeRemaining?.text = getTimeRemainingString(millisRemaining, R.string.idv_timer_label)
        }
        override fun onFinish() {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DocumentSubmissionTimer.getInstance().removeObserver(mTimerObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogDocumentSelectBinding.inflate(inflater, container, false).apply {
            theme = appTheme
            languageProvider = mLanguageProvider
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val documentsAdapter = DocumentAdapter(appTheme, this)
        binding.apply {
            listDocuments.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = documentsAdapter
            }
            DocumentSubmissionTimer.getInstance().addObserver(mTimerObserver)
            binding.btnCancel.setOnClickListener { onBackPressed() }
        }
    }

    override fun getTheme() = R.style.DialogTheme

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    override fun onBackPressed() {
        onDocumentSelected(null)
    }

    override fun onDocumentSelected(document: DocumentClass?) {
        documentCallback.onDocumentSelected(document)
        dismiss()
    }

    companion object {

        @JvmStatic fun newInstance(
            appTheme: AppThemeResponse?,
            languagePresenter: LanguagePackProviderContract,
            documentCallback: DocumentCallback
        ): DocumentSelectFragment {
            return DocumentSelectFragment(appTheme, languagePresenter, documentCallback)
        }
    }
}