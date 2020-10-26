package com.wix.reactnativenotifications.core.actions;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.wix.reactnativenotifications.R;

public class UnlockActivity extends AppCompatActivity {

    public static final String PROMPT_UNLOCK_ACTION = "prompt_unlock";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_unlock);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(PROMPT_UNLOCK_ACTION)) {
            KeyguardManager km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                km.requestDismissKeyguard(this, new KeyguardManager.KeyguardDismissCallback() {
                    @Override
                    public void onDismissError() {
                        UnlockActivity.this.finish();
                    }

                    @Override
                    public void onDismissSucceeded() {
                        UnlockActivity.this.finish();
                    }

                    @Override
                    public void onDismissCancelled() {
                        UnlockActivity.this.finish();
                    }
                });
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                UnlockActivity.this.finish();
            }
        }
    }
}