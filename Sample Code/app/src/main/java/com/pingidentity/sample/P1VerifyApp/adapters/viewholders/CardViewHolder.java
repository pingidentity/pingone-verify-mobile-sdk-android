package com.pingidentity.sample.P1VerifyApp.adapters.viewholders;

import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.callbacks.DocumentClickListener;
import com.pingidentity.sample.P1VerifyApp.databinding.CardItemBinding;
import com.pingidentity.sample.P1VerifyApp.models.CardItem;

public class CardViewHolder extends RecyclerView.ViewHolder {

    private final CardItemBinding binding;

    public CardViewHolder(CardItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(CardItem item, DocumentClickListener callback) {
        switch (item.getCardType()) {
            case LICENSE:
                binding.txtCardTitle.setText(R.string.document_driver_license);
                break;
            case SELFIE:
                binding.txtCardTitle.setText(R.string.document_id_card);
                break;
            case PASSPORT:
                binding.txtCardTitle.setText(R.string.document_passport);
                break;
        }
        binding.cardFrontImage.setImageBitmap(item.getPhotoFrontSide());
        binding.layoutDocument.setOnClickListener(v -> callback.onActionClick(item.getCardType()));
    }

}