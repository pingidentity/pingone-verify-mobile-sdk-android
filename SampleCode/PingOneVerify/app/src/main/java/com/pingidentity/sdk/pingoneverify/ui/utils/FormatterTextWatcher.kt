package com.pingidentity.sdk.pingoneverify.ui.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.pingidentity.sdk.pingoneverify.models.Constants.SPACE
import com.pingidentity.sdk.pingoneverify.neo.listeners.BooleanCallback
import java.text.DecimalFormat

class FormatterTextWatcher(
    private val editText: EditText,
    private val callback: BooleanCallback,
) : TextWatcher {
    private var updatedManually = false

    init {
        callback.onResult(false)
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable) {
        if (updatedManually) {
            updatedManually = false
        } else {
            getFormattedNumber(s.toString()).toLongOrNull()?.let { number ->
                val text = DecimalFormat("#,####").format(number).replace(",", SPACE)
                if (text != s.toString()) {
                    updatedManually = true
                    editText.apply {
                        setText(text)
                        setSelection(text.length)
                    }
                }
            }
            callback.onResult(getFormattedNumber(s.toString()).toLongOrNull() != null)
        }
    }

    companion object {
        private val REGEX_SPACE = "\\s".toRegex()

        @JvmStatic
        fun getFormattedNumber(documentNumber: String): String {
            return documentNumber.replace(REGEX_SPACE, "")
        }
    }
}