package com.pingidentity.sample.P1VerifyApp.models;

public class WizardItem {

    private DocumentType itemType;
    private String itemTitle;
    private String description;
    private String actionText;
    private String moreInfoTitle;
    private String moreInfoText;
    private String moreInfoAction;
    private int itemIcon;

    public WizardItem(DocumentType itemType, String itemTitle, String description, String actionText, String moreInfoTitle, String moreInfoText, String moreInfoAction, int itemIcon) {
        this.itemType = itemType;
        this.itemTitle = itemTitle;
        this.description = description;
        this.actionText = actionText;
        this.moreInfoTitle = moreInfoTitle;
        this.moreInfoText = moreInfoText;
        this.moreInfoAction = moreInfoAction;
        this.itemIcon = itemIcon;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getActionText() {
        return actionText;
    }

    public String getMoreInfoTitle() {
        return moreInfoTitle;
    }

    public String getMoreInfoText() {
        return moreInfoText;
    }

    public String getMoreInfoAction() {
        return moreInfoAction;
    }

    public int getItemIcon() {
        return itemIcon;
    }

    public DocumentType getItemType() {
        return itemType;
    }

    @Override
    public String toString() {
        return "WizardItem{" +
                "itemTitle='" + itemTitle + '\'' +
                ", description='" + description + '\'' +
                ", actionText='" + actionText + '\'' +
                ", moreInfoTitle='" + moreInfoTitle + '\'' +
                ", moreInfoText='" + moreInfoText + '\'' +
                ", moreInfoAction='" + moreInfoAction + '\'' +
                '}';
    }
}
