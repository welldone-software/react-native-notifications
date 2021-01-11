package com.wix.reactnativenotifications.background;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.wix.reactnativenotifications.R;

public class SetLockDialog extends DialogFragment {

    public static final String TAG = SetLockDialog.class.getSimpleName();

    public static SetLockDialog newInstance() {
        return new SetLockDialog();
    }

    public SetLockDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.set_lock_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button settingsButton = view.findViewById(R.id.set_lock_title);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLockSettings();
            }
        });

        Button cancelButton = view.findViewById(R.id.set_lock_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                safelyDismiss();
            }
        });
    }

    private void openLockSettings() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
        Activity activity = getActivity();
        if (activity != null) {
            activity.startActivity(intent);
            dismiss();
            activity.finish();
        }
    }

    private void safelyDismiss() {
        Activity activity = getActivity();
        if (activity != null) {
            dismiss();
            activity.finish();
        }
    }
}