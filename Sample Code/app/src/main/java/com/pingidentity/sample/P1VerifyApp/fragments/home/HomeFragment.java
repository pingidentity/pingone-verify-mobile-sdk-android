package com.pingidentity.sample.P1VerifyApp.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.azoft.carousellayoutmanager.CarouselLayoutManager;
import com.azoft.carousellayoutmanager.CenterScrollListener;
import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.adapters.CardsListAdapter;
import com.pingidentity.sample.P1VerifyApp.callbacks.CustomCarouselListener;
import com.pingidentity.sample.P1VerifyApp.callbacks.DocumentDeleteListener;
import com.pingidentity.sample.P1VerifyApp.callbacks.QRClickListener;
import com.pingidentity.sample.P1VerifyApp.databinding.FragmentHomeBinding;
import com.pingidentity.sample.P1VerifyApp.fragments.details.DetailsFragment;
import com.pingidentity.sample.P1VerifyApp.fragments.qrscanner.ScannerFragment;
import com.pingidentity.sample.P1VerifyApp.models.CardItem;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment implements HomeContract.View, QRClickListener, DocumentDeleteListener {

    private CardsListAdapter mCardsListAdapter;
    private List<CardItem> mDocuments;

    private FragmentHomeBinding mBinding;

    private HomePresenter mPresenter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentHomeBinding.inflate(inflater, container, false);
        mDocuments = new ArrayList<>();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(mBinding.app.toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

        setDrawer();

        mBinding.app.imgScanQr.setOnClickListener(v -> mPresenter.onQRScanClicked());

        setCardsListAdapter();

        mPresenter = new HomePresenter(this);
        mPresenter.initScreen();

        return mBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public void onDocumentDeleted() {
        mPresenter.dismissValidation();
        mPresenter.initScreen();
    }

    //////////////////////////////////////////
    /////////// Navigation drawer ////////////
    //////////////////////////////////////////

    private void setDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(getActivity(), mBinding.drawerLayout, mBinding.app.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mBinding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        setupDrawerListener();
    }

    private void setupDrawerListener() {
        mBinding.btnCheckValidation.setOnClickListener(v -> {
            mBinding.drawerLayout.closeDrawers();
            mPresenter.checkValidation(getActivity());
        });
        mBinding.btnAddSelfie.setOnClickListener(v -> {
            mBinding.drawerLayout.closeDrawers();
            showAlert(
                    getString(R.string.settings_alert_selfie_title),
                    getString(R.string.settings_alert_selfie_message),
                    () -> mPresenter.updateSelfie(getActivity())
            );
        });
        mBinding.btnAddLicense.setOnClickListener(v -> {
            mBinding.drawerLayout.closeDrawers();
            showAlert(
                    getString(R.string.settings_alert_license_title),
                    getString(R.string.settings_alert_license_message),
                    () -> mPresenter.updateLicense(getActivity())
            );
        });
        mBinding.btnAddPassport.setOnClickListener(v -> {
            mBinding.drawerLayout.closeDrawers();
            showAlert(
                    getString(R.string.settings_alert_passport_title),
                    getString(R.string.settings_alert_passport_message),
                    () -> mPresenter.updatePassport(getActivity())
            );
        });
    }

    //////////////////////////////////////////
    /////////// RecyclerView work ////////////
    //////////////////////////////////////////

    private void setCardsListAdapter() {
        mCardsListAdapter = new CardsListAdapter(mDocuments,
                itemType -> {
                    DetailsFragment fragment = DetailsFragment.newInstance(itemType);
                    fragment.setupDeleteListener(this);
                    navigateTo(fragment);
                });
        final CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.VERTICAL, false);
        layoutManager.setPostLayoutListener(new CustomCarouselListener());

        mBinding.app.cardsList.setLayoutManager(layoutManager);
        mBinding.app.cardsList.addOnScrollListener(new CenterScrollListener());
        mBinding.app.cardsList.setHasFixedSize(true);
        mBinding.app.cardsList.setAdapter(mCardsListAdapter);
        mCardsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showDocuments(List<CardItem> data) {
        mDocuments.clear();
        mDocuments.addAll(data);
        mCardsListAdapter.notifyDataSetChanged();
    }

    //////////////////////////////////////////
    /////////// Validation work //////////////
    //////////////////////////////////////////

    @Override
    public void showValidationSuccess() {
        mBinding.app.viewValidationStatus.showValidationSuccess();
    }

    @Override
    public void showValidationError() {
        mBinding.app.viewValidationStatus.showValidationError(() -> showAlert(R.string.validation_failed_title, R.string.validation_failed_description));
    }

    @Override
    public void showValidationInProgress() {
        mBinding.app.viewValidationStatus.showValidationInProgress();
    }

    @Override
    public void hideValidation() {
        mBinding.app.viewValidationStatus.hideValidation();
    }

    //////////////////////////////////////////
    /////////// On scanner result ////////////
    //////////////////////////////////////////

    @Override
    public void onUrlResult(String url) {
        mPresenter.validateByUrl(getActivity(), url);
    }

    @Override
    public void onCodeResult(String code) {
        mPresenter.validateByCode(getActivity(), code);
    }

    //////////////////////////////////////////
    /////////// Navigation ///////////////////
    //////////////////////////////////////////

    public void navigateToQR() {
        ScannerFragment scannerFragment = new ScannerFragment();
        scannerFragment.setupListener(this);
        navigateTo(scannerFragment);
    }

    private void navigateTo(Fragment fragment) {
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    //////////////////////////////////////////
    /////////// Alerts work///////////////////
    //////////////////////////////////////////

    @Override
    public void showAlert(int title, int message) {
        showAlert(getString(title), getString(message), null);
    }

    @Override
    public void showAlert(String message) {
        showAlert(getString(R.string.error_title), message, null);
    }

    private void showAlert(String title, String message, final Runnable positiveAnswer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(R.string.home_cancel, (dialog, which) -> dialog.dismiss());
        if (positiveAnswer != null) {
            builder.setPositiveButton(R.string.home_continue, (dialog, which) -> {
                positiveAnswer.run();
                dialog.dismiss();
            });
        }
        builder.show();
    }

}