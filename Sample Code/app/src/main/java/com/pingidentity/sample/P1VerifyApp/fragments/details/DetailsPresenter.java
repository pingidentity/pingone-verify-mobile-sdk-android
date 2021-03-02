package com.pingidentity.sample.P1VerifyApp.fragments.details;

import android.graphics.Bitmap;

import com.pingidentity.p1verifyidschema.DriverLicense;
import com.pingidentity.p1verifyidschema.Passport;
import com.pingidentity.p1verifyidschema.Selfie;
import com.pingidentity.sample.P1VerifyApp.R;
import com.pingidentity.sample.P1VerifyApp.di.Injector;
import com.pingidentity.sample.P1VerifyApp.models.DetailItem;
import com.pingidentity.sample.P1VerifyApp.models.DocumentType;
import com.pingidentity.sample.P1VerifyApp.storage.CardRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class DetailsPresenter implements DetailsContract.Presenter {

    private DetailsContract.View mView;
    private DocumentType mDocumentType;

    @Inject
    CardRepository mRepository;

    public DetailsPresenter(DetailsContract.View view) {
        this.mView = view;
        Injector.getAppComponent().inject(this);
    }

    @Override
    public void initScreen(DocumentType type) {
        int title = 0;
        List<Bitmap> images = new ArrayList<>();
        List<DetailItem> details = new ArrayList<>();

        this.mDocumentType = type;

        switch (mDocumentType) {
            case SELFIE:
                Selfie selfie = mRepository.getIdCard();
                title = R.string.document_id_card;
                images.add(selfie.getFrontImage());
                break;
            case LICENSE:
                DriverLicense driverLicense = mRepository.getDriverLicense();
                title = R.string.document_driver_license;
                images.add(driverLicense.getFrontImage());
                images.add(driverLicense.getBackImage());
                details.add(new DetailItem("Name", driverLicense.getFirstName() + " " + driverLicense.getMiddleName() + " " + driverLicense.getLastName(), true));
                details.add(new DetailItem("Address", driverLicense.getAddressStreet() + " " + driverLicense.getAddressCity() + " " + driverLicense.getAddressState() + " " + driverLicense.getAddressZip(), true));
                details.add(new DetailItem("ID number", driverLicense.getIdNumber(), true));
                details.add(new DetailItem("Country", driverLicense.getCountry(), false));
                details.add(new DetailItem("Gender", driverLicense.getGender(), false));
                details.add(new DetailItem("Hair color", driverLicense.getHairColor(), false));
                details.add(new DetailItem("Eye color", driverLicense.getEyeColor(), false));
                details.add(new DetailItem("Height", driverLicense.getHeight(), false));
                details.add(new DetailItem("Weight", driverLicense.getWeight(), false));
                break;
            case PASSPORT:
                Passport passport = mRepository.getPassport();
                title = R.string.document_passport;
                images.add(passport.getFrontImage());
                break;
        }

        mView.setTitle(title);
        mView.showPhotos(images);
        mView.showInfo(details);
    }

    @Override
    public void deleteCard() {
        switch (mDocumentType) {
            case SELFIE:
                mRepository.deleteCard(mRepository.getIdCard());
                break;
            case LICENSE:
                mRepository.deleteCard(mRepository.getDriverLicense());
                break;
            case PASSPORT:
                mRepository.deleteCard(mRepository.getPassport());
                break;
        }
        mView.onCardDeleted();
    }

    @Override
    public void onDestroy() {
        mView = null;
    }
}
