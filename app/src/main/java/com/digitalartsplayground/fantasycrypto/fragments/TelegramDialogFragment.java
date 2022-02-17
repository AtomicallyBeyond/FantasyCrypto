package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.digitalartsplayground.fantasycrypto.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TelegramDialogFragment extends DialogFragment {

    private ImageView telegramImage;
    private ImageButton closeButton;

    public static TelegramDialogFragment getInstance() {
        TelegramDialogFragment telegramDialogFragment = new TelegramDialogFragment();
        return telegramDialogFragment;
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.telegram_layout, null);
        closeButton = view.findViewById(R.id.telegram_close_button);
        telegramImage = view.findViewById(R.id.telegram_image);
        setListeners();
        return view;
    }

    private void setListeners() {
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Objects.requireNonNull(getDialog()).dismiss();
            }
        });

        telegramImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("https://t.me/fantasy_crypto");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                Objects.requireNonNull(getDialog()).dismiss();
            }
        });
    }
}
