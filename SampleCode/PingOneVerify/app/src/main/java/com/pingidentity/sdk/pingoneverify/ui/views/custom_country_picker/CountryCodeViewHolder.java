package com.pingidentity.sdk.pingoneverify.ui.views.custom_country_picker;

import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sdk.pingoneverify.ui.Country;
import com.pingidentity.sdk.pingoneverify.sample.databinding.ItemCountryCodeBinding;

public class CountryCodeViewHolder extends RecyclerView.ViewHolder {

    private final ItemCountryCodeBinding mBinding;

    public CountryCodeViewHolder(ItemCountryCodeBinding binding) {
        super(binding.getRoot());
        this.mBinding = binding;
    }

    void bind(Country country) {
        mBinding.txtCountry.setText(country.getName());
        mBinding.txtCode.setText(country.getDialCode());
    }

}
