package com.digitalartsplayground.fantasycrypto.persistence;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DoubleListHolder {
    public List<Double> doubleList;

    @NonNull
    @NotNull
    @Override
    public String toString() {
        Gson gson = new Gson();
        String json = gson.toJson(doubleList);
        return json;
    }
}
