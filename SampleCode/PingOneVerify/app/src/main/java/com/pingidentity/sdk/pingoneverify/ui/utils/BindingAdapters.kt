package com.pingidentity.sdk.pingoneverify.ui.utils

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse
import com.pingidentity.sdk.pingoneverify.neo.models.theme.ImageLink
import com.pingidentity.sdk.pingoneverify.sample.R
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract

object BindingAdapters {

    private fun resolveString(context: Context, provider: LanguagePackProviderContract?, resourceId: Int): String {
        val default = context.getString(resourceId)
        return provider?.getStringForResource(resourceId, default) ?: default
    }

    // Called from XML layouts via app:languageProvider / app:resourceId
    // requireAll = false — languageProvider is optional; falls back to context.getString when null.
    @JvmStatic
    @BindingAdapter(value = ["app:languageProvider", "app:resourceId"], requireAll = false)
    fun setProviderText(
        textView: TextView,
        provider: LanguagePackProviderContract?,
        resourceId: Int = 0
    ) {
        if (resourceId == 0) return
        textView.text = resolveString(textView.context, provider, resourceId)
    }

    @JvmStatic
    @BindingAdapter(value = ["app:languageProviderHint", "app:hintResourceId"], requireAll = false)
    fun setProviderHint(
        textView: TextView,
        provider: LanguagePackProviderContract?,
        resourceId: Int = 0
    ) {
        if (resourceId == 0) return
        textView.hint = resolveString(textView.context, provider, resourceId)
    }

    @JvmStatic
    @BindingAdapter("bind:icon_tint")
    fun setIconTint(view: ImageView, theme: AppThemeResponse?) {
        val color = theme?.iconTintColorRaw ?: theme?.headingTextColorRaw ?: return
        view.imageTintList = ColorStateList.valueOf(color.toColorInt())
    }

    @JvmStatic
    @BindingAdapter("bind:logo_tint")
    fun setLogoTint(view: ImageView, theme: AppThemeResponse?) {
        val color = (theme?.foregroundMainColorRaw ?: theme?.solidButtonAppearance?.backgroundColorRaw) ?: return
        view.setColorFilter(color.toColorInt(), PorterDuff.Mode.SRC_ATOP)
    }

    @JvmStatic
    @BindingAdapter("bind:image_url")
    fun loadImage(view: View, imageLink: ImageLink?) {
        val imageView = view.findViewById<ImageView>(R.id.heading_logo)
        val textView = view.findViewById<TextView>(R.id.heading_text)
        if (imageLink != null && imageLink.image != null && imageLink.image.isNotEmpty()) {
            Glide.with(imageView.context)
                .asBitmap()
                .load(Base64.decode(imageLink.image, Base64.DEFAULT))
                .fitCenter()
                .into(imageView)
        } else {
            val logoIdentifier = imageView.context.resources.getIdentifier("idv_logo", "drawable", imageView.context.packageName)
            if (logoIdentifier != 0) {
                Glide.with(imageView.context).load(logoIdentifier).fitCenter().into(imageView)
            } else if (imageLink?.href != null && imageLink.href.isNotEmpty()) {
                Glide.with(imageView.context).load(imageLink.href).fitCenter().into(imageView)
            } else {
                Glide.with(imageView.context).load(R.drawable.sdk_logo).fitCenter().into(imageView)
            }
        }
        textView.visibility = View.GONE
        imageView.visibility = View.VISIBLE
    }
}
