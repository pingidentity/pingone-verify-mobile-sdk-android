package com.pingidentity.sdk.pingoneverify.sample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pingidentity.sdk.pingoneverify.sample.MainActivity;
import com.pingidentity.sdk.pingoneverify.sample.R;
import com.pingidentity.sdk.pingoneverify.ui.providers.PingOneVerifyHelper;
import com.pingidentity.sdk.pingoneverify.sample.qr_scanner.QrScannerDialog;

public class MainFragment extends Fragment {

    public static final String TAG = MainFragment.class.getName();

    private Button mBtnVerify;
    private View waitOverlay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnVerify = view.findViewById(R.id.btn_verify);
        waitOverlay = view.findViewById(R.id.wait_spinner);
        mBtnVerify.setOnClickListener(mView -> initPingOneClient());
    }

    private void initPingOneClient() {
        QrScannerDialog scanner = QrScannerDialog.newInstance(new QrScannerDialog.Listener() {
            @Override
            public void onQrScanned(String verificationUrl) {
                startVerification(verificationUrl);
            }

            @Override
            public void onQrCanceled() {
                setInProgress(false);
            }
        });
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(android.R.id.content, scanner, QrScannerDialog.TAG)
                .addToBackStack(null)
                .commit();
    }

    private void startVerification(String verificationUrl) {
        setInProgress(true);
        if (!(getActivity() instanceof MainActivity)) return;
        new PingOneVerifyHelper((MainActivity) getActivity(), verificationUrl);
    }

    /*
     * Example: Custom UI integration — implement VerifyTransactionCoordinatorDelegate for lifecycle callbacks.
     * Pass a custom VerifyTransactionCoordinatorDelegate via setCoordinatorDelegate() to replace the built-in UI.
     *
     * private void initPingOneClientCustomUI(String verificationUrl) {
     *     new PingOneVerifyClient.Builder(verificationUrl)
     *             .setContext(getActivity())
     *             .setCoordinatorDelegate(myCustomCoordinator)
     *             .build(callback);
     * }
     */


    private void setInProgress(boolean inProgress) {
        mBtnVerify.setEnabled(!inProgress);
        mBtnVerify.setAlpha(inProgress ? 0.4f : 1f);
        waitOverlay.setVisibility(inProgress ? View.VISIBLE : View.GONE);
    }

}
