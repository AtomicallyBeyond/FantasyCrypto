package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.digitalartsplayground.fantasycrypto.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RewardDialogFragment extends DialogFragment {

    private final static String AMOUNT_ARG = "amountArg";

    private TextView amount;
    private ImageButton closeButton;
    private String amountString;

    public static RewardDialogFragment getInstance(String rewardAmount) {
        RewardDialogFragment rewardDialogFragment = new RewardDialogFragment();

        Bundle args = new Bundle();
        args.putString(AMOUNT_ARG, rewardAmount);
        rewardDialogFragment.setArguments(args);
        return rewardDialogFragment;
    }


    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        amountString = args.getString(AMOUNT_ARG);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reward_layout, null);
        closeButton = view.findViewById(R.id.reward_close_button);
        amount = view.findViewById(R.id.reward_amount);
        setCloseButtonListener();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        amount.setText(amountString);
    }

    private void setCloseButtonListener() {
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Objects.requireNonNull(getDialog()).dismiss();
            }
        });
    }
}
