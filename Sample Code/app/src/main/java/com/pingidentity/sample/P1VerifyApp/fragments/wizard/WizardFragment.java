package com.pingidentity.sample.P1VerifyApp.fragments.wizard;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.activities.HomeActivity;
import com.pingidentity.sample.P1VerifyApp.adapters.WizardAdapter;
import com.pingidentity.sample.P1VerifyApp.models.WizardItem;
import com.pingidentity.sample.P1VerifyApp.databinding.FragmentWizardBinding;

import java.util.ArrayList;
import java.util.List;

public class WizardFragment extends Fragment implements WizardContract.View {

    private WizardContract.Presenter mPresenter;

    private FragmentWizardBinding binding;

    private WizardAdapter pagerAdapter;
    private List<WizardItem> mWizardData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWizardBinding.inflate(inflater, container, false);
        mPresenter = new WizardPresenter(this);

        setupViewPager();
        setupOnClickListeners();

        mPresenter.getWizardData();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.onDestroy();
        binding = null;
    }

    private void setupOnClickListeners() {
        binding.toolbar.toolbarCancel.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.toolbar.toolbarSkip.setOnClickListener(v -> mPresenter.onSkip());
    }

    private void setupViewPager() {
        mWizardData = new ArrayList<>();
        pagerAdapter = new WizardAdapter(getContext(), mWizardData, itemType -> {
            switch (itemType) {
                case SELFIE:
                    mPresenter.captureSelfie(getActivity());
                    break;
                case LICENSE:
                    mPresenter.captureLicense(getActivity());
                    break;
                case PASSPORT:
                    mPresenter.capturePassport(getActivity());
                    break;
            }
        });
        binding.viewPager.setAdapter(pagerAdapter);
        binding.pagerIndicator.setViewPager(binding.viewPager);
        pagerAdapter.registerAdapterDataObserver(binding.pagerIndicator.getAdapterDataObserver());
    }

    @Override
    public void showWizard(List<WizardItem> wizardData) {
        mWizardData.addAll(wizardData);
        pagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void nextStep() {
        binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
    }

    @Override
    public void showAlert(int title, int message) {
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.wizard_ok, (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void finishWizard() {
        Intent myIntent = new Intent(getActivity(), HomeActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getActivity().startActivity(myIntent);
    }

}