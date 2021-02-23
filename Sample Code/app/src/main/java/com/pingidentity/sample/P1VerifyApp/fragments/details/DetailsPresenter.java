package com.pingidentity.sample.P1VerifyApp.fragments.details;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

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
                driverLicense = driverLicense.getFormattedDriverLicense();
                details.add(new DetailItem("Name", getFullName(driverLicense.getFirstName(), driverLicense.getLastName()), true));
                details.add(new DetailItem("Address", driverLicense.getAddressStreet() + " " + driverLicense.getAddressCity() + " " + driverLicense.getAddressState() + " " + driverLicense.getAddressZip(), true));
                details.add(new DetailItem("ID number", driverLicense.getIdNumber(), true));
                details.add(new DetailItem("Expiration Date", driverLicense.getExpirationDate(), false));
                details.add(new DetailItem("Birth Date", driverLicense.getBirthDate(), false));
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
                if (passport.getFirstName() != null || passport.getLastName() != null)
                    details.add(new DetailItem("Name", getFullName(passport.getFirstName(), passport.getLastName()), true));
                if (passport.getIdNumber() != null)
                    details.add(new DetailItem("ID number", passport.getIdNumber(), true));
                if (passport.getCountry() != null)
                    details.add(new DetailItem("Country", passport.getCountry(), false));
                if (passport.getGender() != null)
                    details.add(new DetailItem("Gender", passport.getGender(), false));
                if (passport.getBirthDate() != null)
                    details.add(new DetailItem("Birth date", passport.getBirthDate(), false));
                if (passport.getNationality() != null)
                    details.add(new DetailItem("Nationality", passport.getNationality(), false));
                if (passport.getPersonalNumber() != null)
                    details.add(new DetailItem("Personal Number", passport.getPersonalNumber(), false));
                if (passport.getExpirationDate() != null)
                    details.add(new DetailItem("Expiration Date", passport.getExpirationDate(), false));
                break;
        }

        mView.setTitle(title);
        mView.showPhotos(images);
        mView.showInfo(details);
    }

    private String getFullName(@Nullable final String firstName, @Nullable final String lastName) {
        final StringBuilder sb = new StringBuilder();
        if (firstName != null && !firstName.isEmpty()) {
            sb.append(firstName).append(" ");
        }
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(lastName);
        }
        return sb.toString();
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
