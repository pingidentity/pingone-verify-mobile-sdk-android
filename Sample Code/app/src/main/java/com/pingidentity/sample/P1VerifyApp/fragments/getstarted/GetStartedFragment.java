package com.pingidentity.sample.P1VerifyApp.fragments.getstarted;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pingidentity.sample.P1VerifyApp.R;

import com.pingidentity.sample.P1VerifyApp.databinding.FragmentGetStartedBinding;
import com.pingidentity.sample.P1VerifyApp.fragments.wizard.WizardFragment;

public class GetStartedFragment extends Fragment {

    private FragmentGetStartedBinding binding;

    public GetStartedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGetStartedBinding.inflate(inflater, container, false);
        binding.button.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.fragment_container, new WizardFragment())
                    .commit();
        });
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}