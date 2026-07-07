package com.pingidentity.sdk.pingoneverify.ui.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pingidentity.sdk.pingoneverify.models.DocumentModel
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse
import com.pingidentity.sdk.pingoneverify.neo.models.DocumentClass
import com.pingidentity.sdk.pingoneverify.sample.R
import com.pingidentity.sdk.pingoneverify.sample.databinding.ItemDocumentTypeBinding

class DocumentAdapter(
    private val appTheme: AppThemeResponse?,
    private val callback: DocumentCallback,
) : RecyclerView.Adapter<DocumentAdapter.DocumentHolder>() {
    private val documents = getDocumentTypes()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentHolder {
        val itemBinding = ItemDocumentTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
            theme = appTheme
        }
        return DocumentHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: DocumentHolder, position: Int) {
        val documentData = documents[position]
        holder.setData(documentData)
        holder.itemView.setOnClickListener {
            callback.onDocumentSelected(documentData.documentType)
        }
    }

    override fun getItemCount() = documents.size

    class DocumentHolder(
        private val binding: ItemDocumentTypeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun setData(document: DocumentModel) {
            binding.btnDocument.setText(document.textId)
        }
    }

    companion object {
        @JvmStatic
        fun getDocumentTypes(): List<DocumentModel> {
            return listOf(
                DocumentModel(DocumentClass.DRIVER_LICENSE, R.string.sdk_documentCapture_option_license),
                DocumentModel(DocumentClass.PASSPORT, R.string.sdk_documentCapture_option_passport),
                DocumentModel(DocumentClass.PASSPORT_CARD, R.string.sdk_documentCapture_option_card),
                DocumentModel(DocumentClass.OTHER, R.string.sdk_documentCapture_option_other)
            )
        }
    }
}

interface DocumentCallback {
    fun onDocumentSelected(document: DocumentClass?)
}