package com.digitalartsplayground.fantasycrypto.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CoinDescription {

    @SerializedName("en")
    @Expose
    private String englishDescription;

    public String getEnglishDescription() {
        return englishDescription;
    }

    public void setEnglishDescription(String englishDescription) {
        this.englishDescription = englishDescription;

    }
}
