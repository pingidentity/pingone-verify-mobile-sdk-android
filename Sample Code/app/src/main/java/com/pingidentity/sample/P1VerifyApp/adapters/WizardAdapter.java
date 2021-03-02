package com.pingidentity.sample.P1VerifyApp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sample.P1VerifyApp.adapters.viewholders.WizardViewHolder;
import com.pingidentity.sample.P1VerifyApp.callbacks.WizardClickListener;
import com.pingidentity.sample.P1VerifyApp.databinding.WizardItemBinding;
import com.pingidentity.sample.P1VerifyApp.fragments.wizard.WizardFragment;
import com.pingidentity.sample.P1VerifyApp.models.WizardItem;

import java.util.List;

public class WizardAdapter extends RecyclerView.Adapter<WizardViewHolder> {

    private List<WizardItem> mData;
    private LayoutInflater mInflater;
    WizardClickListener clickListener;

    public WizardAdapter(Context context, List<WizardItem> data , WizardClickListener clickListener) {
        this.mInflater = LayoutInflater.from(context);
        this.clickListener = clickListener;
        this.mData = data;
    }

    @Override
    public WizardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        WizardItemBinding binding = WizardItemBinding.inflate(mInflater, parent, false);
        return new WizardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(WizardViewHolder holder, int position) {
        holder.bind(mData.get(position), clickListener);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

}

