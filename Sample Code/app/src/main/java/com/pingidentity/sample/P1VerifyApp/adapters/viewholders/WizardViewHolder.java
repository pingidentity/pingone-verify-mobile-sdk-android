package com.pingidentity.sample.P1VerifyApp.adapters.viewholders;

import android.app.AlertDialog;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.callbacks.WizardClickListener;
import com.pingidentity.sample.P1VerifyApp.databinding.WizardItemBinding;
import com.pingidentity.sample.P1VerifyApp.fragments.wizard.WizardFragment;
import com.pingidentity.sample.P1VerifyApp.models.WizardItem;

public class WizardViewHolder extends RecyclerView.ViewHolder {

    private final WizardItemBinding binding;

    public WizardViewHolder(WizardItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(WizardItem item, WizardClickListener clickListener) {
        binding.txtTitle.setText(item.getItemTitle());
        binding.txtDescription.setText(item.getDescription());
        binding.btnAction.setText(item.getActionText());
        Glide
                .with(binding.imgBadge.getContext())
                .load(item.getItemIcon())
                .into(binding.imgBadge);
        binding.btnMoreInfo.setOnClickListener(v -> showDialog(item));
        binding.btnAction.setOnClickListener(v -> clickListener.onActionClick(item.getItemType()));
    }

    private void showDialog(WizardItem item){
        new AlertDialog.Builder(binding.getRoot().getContext())
                .setTitle(binding.getRoot().getContext().getString(R.string.wizard_info_title))
                .setMessage(item.getMoreInfoText())
                .setPositiveButton(R.string.wizard_ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

}
