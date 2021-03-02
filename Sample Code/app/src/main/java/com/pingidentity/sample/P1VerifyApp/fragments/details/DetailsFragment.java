package com.pingidentity.sample.P1VerifyApp.fragments.details;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pingidentity.sample.P1VerifyApp.adapters.DetailsAdapter;
import com.pingidentity.sample.P1VerifyApp.callbacks.DocumentDeleteListener;
import com.pingidentity.sample.P1VerifyApp.databinding.FragmentDetailsBinding;
import com.pingidentity.sample.P1VerifyApp.models.DetailItem;
import com.pingidentity.sample.P1VerifyApp.models.DocumentType;
import com.pingidentity.sample.P1VerifyApp.views.DetailView;

import java.util.ArrayList;
import java.util.List;


public class DetailsFragment extends Fragment implements DetailsContract.View {

    private FragmentDetailsBinding mBinding;
    private DetailsAdapter mPagerAdapter;

    private DocumentDeleteListener mDeleteCallback;

    private List<Bitmap> mPhotos;

    private DetailsContract.Presenter mPresenter;


    public static DetailsFragment newInstance(DocumentType type) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    public DetailsFragment() {
        // Required empty public constructor
    }

    public void setupDeleteListener(DocumentDeleteListener listener) {
        this.mDeleteCallback = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentDetailsBinding.inflate(inflater, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();

        mBinding.toolbar.toolbarHome.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        mPresenter = new DetailsPresenter(this);

        DocumentType type = (DocumentType) getArguments().getSerializable("type");

        initPhotosRecycler();

        mPresenter.initScreen(type);

        mBinding.btnDeleteCard.setOnClickListener(v -> mPresenter.deleteCard());

        return mBinding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    @Override
    public void setTitle(int title) {
        mBinding.txtDocumentName.setText(title);
    }

    @Override
    public void showPhotos(List<Bitmap> data) {
        if (data.size() < 2) mBinding.pagerIndicator.setVisibility(View.GONE);
        mPhotos.clear();
        mPhotos.addAll(data);
        mPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void showInfo(List<DetailItem> data) {
        if (data == null || data.isEmpty()) return;
        for (DetailItem item : data) {
            mBinding.layoutDetails.addView(new DetailView(getContext(), item.getParamName(), item.getParamInfo(), item.isUnderline()));
        }
    }

    @Override
    public void onCardDeleted() {
        if (mDeleteCallback != null) {
            mDeleteCallback.onDocumentDeleted();
        }
        getParentFragmentManager().popBackStack();
    }

    private void initPhotosRecycler() {
        mPhotos = new ArrayList<>();
        mPagerAdapter = new DetailsAdapter(getContext(), mPhotos);
        mBinding.viewPager.setAdapter(mPagerAdapter);
        mBinding.pagerIndicator.setViewPager(mBinding.viewPager);
        mPagerAdapter.registerAdapterDataObserver(mBinding.pagerIndicator.getAdapterDataObserver());
    }

}