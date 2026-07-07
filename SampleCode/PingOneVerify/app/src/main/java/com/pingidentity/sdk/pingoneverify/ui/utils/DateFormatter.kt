package com.pingidentity.sdk.pingoneverify.ui.utils

import android.content.Context
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract
import java.util.concurrent.TimeUnit

object DateFormatter {

    fun getMinutes(
        context: Context,
        languagePresenter: LanguagePackProviderContract?,
        diff: Int,
        labelId: Int
    ): String? {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff.toLong())
        if (minutes <= 0) return null
        val label = languagePresenter?.getStringForResource(labelId, context.getString(labelId)) ?: return null
        return String.format(label, minutes)
    }

    fun getSeconds(
        context: Context,
        languagePresenter: LanguagePackProviderContract?,
        diff: Int,
        labelId: Int
    ): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff.toLong()) % TimeUnit.MINUTES.toSeconds(1)
        val label = languagePresenter?.getStringForResource(labelId, context.getString(labelId))
            ?: return seconds.toString()
        return String.format(label, seconds)
    }

    fun getTimeDifferenceFormatted(
        context: Context,
        languagePresenter: LanguagePackProviderContract?,
        minLabel: String?,
        secLabel: String,
        labelId: Int
    ): String {
        val value = if (minLabel != null) "$minLabel $secLabel" else secLabel
        val label = languagePresenter?.getStringForResource(labelId, context.getString(labelId))
            ?: return value
        return String.format(label, value)
    }
}
