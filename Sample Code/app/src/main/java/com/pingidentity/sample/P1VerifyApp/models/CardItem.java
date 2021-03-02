package com.pingidentity.sample.P1VerifyApp.models;

import android.graphics.Bitmap;

public class CardItem {

    private DocumentType cardType;
    private Bitmap photoFrontSide;

    public CardItem(DocumentType cardType, Bitmap photoFrontSide){
        this.cardType = cardType;
        this.photoFrontSide = photoFrontSide;
    }

    public DocumentType getCardType() {
        return cardType;
    }

    public Bitmap getPhotoFrontSide() {
        return photoFrontSide;
    }

    @Override
    public String toString() {
        return "CardItem{" +
                "cardType=" + cardType +
                ", photoFrontSide=" + photoFrontSide +
                '}';
    }
}
