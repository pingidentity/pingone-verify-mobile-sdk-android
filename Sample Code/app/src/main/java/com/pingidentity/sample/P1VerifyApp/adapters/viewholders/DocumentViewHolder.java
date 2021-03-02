package com.pingidentity.sample.P1VerifyApp.adapters.viewholders;

import android.graphics.Bitmap;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pingidentity.sample.P1VerifyApp.databinding.ViewHolderDocumentBinding;

public class DocumentViewHolder extends RecyclerView.ViewHolder {

    private final ViewHolderDocumentBinding binding;

    public DocumentViewHolder(ViewHolderDocumentBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(Bitmap item) {
        Glide
                .with(binding.imgDocument.getContext())
                .asBitmap()
                .load(item)
                .into(binding.imgDocument);

    }

}

