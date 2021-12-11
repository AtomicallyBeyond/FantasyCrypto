package com.digitalartsplayground.fantasycrypto.models;

import android.text.util.Linkify;
import android.widget.TextView;

import androidx.room.ColumnInfo;

import com.digitalartsplayground.fantasycrypto.R;
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


/*        String descrption = "\"Cardano is a decentralised platform" +
                " that will allow complex programmable transfers of" +
                " value in a secure and scalable fashion. It is one" +
                " of the first blockchains to be built in the highly" +
                " secure Haskell programming language. Cardano is developing" +
                " a <a href=\\\"https://www.coingecko.com/en?category_id=29\\\">smart contract platform</a>" +
                " which seeks to deliver more advanced features than any protocol previously developed." +
                " It is the first blockchain platform to evolve out of a scientific philosophy and a research-first driven approach." +
                " The development team consists of a large global collective of expert engineers and researchers.\\r\\n\\r\\n" +
                "The Cardano project is different from other blockchain projects as it openly addresses the need for regulatory oversight whilst maintaining" +
                " consumer privacy and protections through an innovative software architecture.";


        TextView textView = findViewById(R.id.description_textview);
        Linkify.addLinks(textView, Linkify.WEB_URLS);*/

    }
}
