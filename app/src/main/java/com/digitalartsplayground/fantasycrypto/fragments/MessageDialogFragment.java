package com.digitalartsplayground.fantasycrypto.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.digitalartsplayground.fantasycrypto.R;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class MessageDialogFragment extends DialogFragment {

    private final static String MESSAGE_ARG = "messageArg";
    private final static String TITLE_ARG = "titleArg";

    private TextView titleView;
    private TextView messageView;
    private ImageButton closeButton;
    private String title;
    private String message;

    public static MessageDialogFragment getInstance(String title, String message) {
        MessageDialogFragment messageDialogFragment = new MessageDialogFragment();

        Bundle args = new Bundle();
        args.putString(TITLE_ARG, title);
        args.putString(MESSAGE_ARG, message);
        messageDialogFragment.setArguments(args);
        return messageDialogFragment;
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
        title = args.getString(TITLE_ARG);
        message = args.getString(MESSAGE_ARG);
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.message_dialog, null);
        closeButton = view.findViewById(R.id.message_dialog_close_button);
        titleView = view.findViewById(R.id.message_dialog_title);
        messageView = view.findViewById(R.id.message_dialog_textview);
        setCloseButtonListener();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        titleView.setText(title);
        messageView.setText(message);
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
