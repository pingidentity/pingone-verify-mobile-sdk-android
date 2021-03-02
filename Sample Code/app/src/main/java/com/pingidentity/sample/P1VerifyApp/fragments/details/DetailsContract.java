package com.pingidentity.sample.P1VerifyApp.fragments.details;

import android.graphics.Bitmap;

import com.pingidentity.sample.P1VerifyApp.models.DetailItem;
import com.pingidentity.sample.P1VerifyApp.models.DocumentType;

import java.util.List;

public class DetailsContract {

    interface View {

        void setTitle(int title);

        void showPhotos(List<Bitmap> data);

        void showInfo(List<DetailItem> data);

        void onCardDeleted();

    }

    interface Presenter {

        void initScreen(DocumentType type);

        void deleteCard();

        void onDestroy();

    }


}
