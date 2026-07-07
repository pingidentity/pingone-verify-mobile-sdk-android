package com.pingidentity.sdk.pingoneverify.ui.views.custom_country_picker;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pingidentity.sdk.pingoneverify.ui.Country;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.sample.databinding.ItemCountryCodeBinding;

import java.util.List;

public class CountryCodesAdapter extends RecyclerView.Adapter<CountryCodeViewHolder> {

    private final List<Country> mCountryCodes;
    private final CustomCountryCodePickerListener mListener;
    private final AppThemeResponse mAppTheme;

    public CountryCodesAdapter(List<Country> countries, AppThemeResponse appTheme, CustomCountryCodePickerListener pickerListener) {
        this.mCountryCodes = countries;
        this.mAppTheme = appTheme;
        this.mListener = pickerListener;
    }

    @NonNull
    @Override
    public CountryCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCountryCodeBinding binding = ItemCountryCodeBinding.inflate(inflater, parent, false);
        binding.setTheme(mAppTheme);
        return new CountryCodeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryCodeViewHolder holder, int position) {
        Country country = mCountryCodes.get(position);
        holder.bind(country);
        holder.itemView.setOnClickListener(view -> mListener.onCountryCodePicked(country));
    }

    @Override
    public int getItemCount() {
        return mCountryCodes.size();
    }
}
