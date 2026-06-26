package com.pingidentity.sdk.pingoneverify.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pingidentity.sdk.pingoneverify.ui.Country;
import com.pingidentity.sdk.pingoneverify.neo.models.AppThemeResponse;
import com.pingidentity.sdk.pingoneverify.sample.R;
import com.pingidentity.sdk.pingoneverify.sample.databinding.DialogCountryPickerBinding;
import com.pingidentity.sdk.pingoneverify.ui.views.custom_country_picker.CountryCodesAdapter;
import com.pingidentity.sdk.pingoneverify.ui.views.custom_country_picker.CustomCountryCodePickerListener;
import com.pingidentity.sdk.provider.language.LanguagePackProviderContract;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CountryPickerDialog extends BaseDialogFragment {
    public static final String TAG = CountryPickerDialog.class.getName();

    private CustomCountryCodePickerListener mListener;

    private DialogCountryPickerBinding mBinding;

    private List<Country> mCountriesList;
    private List<Country> mFilteredCountries;

    public CountryPickerDialog(AppThemeResponse appTheme, LanguagePackProviderContract languageProvider) {
        super(appTheme, languageProvider);
    }

    public static CountryPickerDialog newInstance(AppThemeResponse appTheme, LanguagePackProviderContract languageProvider, List<Country> countries, CustomCountryCodePickerListener listener) {
        CountryPickerDialog countryPickerDialog = new CountryPickerDialog(appTheme, languageProvider);
        countryPickerDialog.mCountriesList = countries;
        countryPickerDialog.mListener = listener;
        return countryPickerDialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DialogCountryPickerBinding.inflate(inflater, container, false);
        mBinding.setTheme(getAppTheme());
        mBinding.setLanguageProvider(getMLanguageProvider());
        init();
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public int getTheme() {
        return R.style.DialogTheme;
    }

    private void init() {
        mFilteredCountries = new ArrayList<>(mCountriesList);
        mBinding.rvCountries.setLayoutManager(new LinearLayoutManager(requireContext()));
        mBinding.rvCountries.setAdapter(getAdapter(mFilteredCountries));
        mBinding.edtDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                List<Country> filteredList = mCountriesList.stream()
                        .filter(country -> country.getDisplayName().toLowerCase().contains(charSequence.toString().toLowerCase()))
                        .collect(Collectors.toList());
                mFilteredCountries.clear();
                mFilteredCountries.addAll(filteredList);
                mBinding.rvCountries.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mBinding.btnCancel.setOnClickListener(view -> mListener.onCountryCodePicked(null));
    }

    private CountryCodesAdapter getAdapter(List<Country> countries) {
        return new CountryCodesAdapter(countries, getAppTheme(), index -> {
            mListener.onCountryCodePicked(index);
        });
    }

}
