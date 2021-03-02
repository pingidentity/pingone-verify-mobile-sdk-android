package com.pingidentity.sample.P1VerifyApp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sample.P1VerifyApp.adapters.viewholders.DocumentViewHolder;
import com.pingidentity.sample.P1VerifyApp.databinding.ViewHolderDocumentBinding;

import java.util.List;

public class DetailsAdapter extends RecyclerView.Adapter<DocumentViewHolder> {

    private List<Bitmap> mData;
    private LayoutInflater mInflater;

    public DetailsAdapter(Context context, List<Bitmap> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolderDocumentBinding binding = ViewHolderDocumentBinding.inflate(mInflater, parent, false);
        return new DocumentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {
        holder.bind(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


}
