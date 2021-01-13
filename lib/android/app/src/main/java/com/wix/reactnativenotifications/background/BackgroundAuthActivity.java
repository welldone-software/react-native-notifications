package com.wix.reactnativenotifications.background;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.wix.reactnativenotifications.Defs;
import com.wix.reactnativenotifications.R;
import com.wix.reactnativenotifications.core.NotificationBackgroundService;
import com.wix.reactnativenotifications.core.actions.ActionPayloadSaver;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BackgroundAuthActivity extends AppCompatActivity {

    public static final String PROMPT_UNLOCK_ACTION = "prompt_unlock";
    public static final String PROMPT_BIOMETRIC_ACTION = "prompt_biometric";

    private Handler mHandler;
    private final Runnable mKillActivity = new Runnable() {
        @Override
        public void run() {
            BackgroundAuthActivity.this.finish();
        }
    };

    private final BiometricPrompt.AuthenticationCallback mAuthCallback = new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            finishActivity();
        }

        @Override
        public void onAuthenticationFailed() {
            finishActivity();
        }

        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            finishActivity();
        }
    };

    private final BroadcastReceiver mUnlockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (!myKM.inKeyguardRestrictedInputMode()) {
                ActionPayloadSaver saver = ActionPayloadSaver.getInstance(context);
                Bundle bundle = saver.getAwaitingAction();
                if (bundle != null) {
                    Intent serviceIntent = new Intent(context, NotificationBackgroundService.class);
                    serviceIntent.setAction(Defs.NOTIFICATION_ACTION_CLICK);
                    serviceIntent.putExtras(bundle);
                    context.startService(serviceIntent);
                    NotificationBackgroundService.acquireWakeLockNow(context);
                }
                saver.clearAwaitingAction();
                BackgroundAuthActivity.this.finish();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mUnlockReceiver, intentFilter);
        startTimeout();
    }

    @Override
    protected void onResume() {
        super.onResume();
        onActionAppears(getIntent());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUnlockReceiver);
        killTimeout();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private void killTimeout() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mKillActivity);
            mHandler = null;
        }
    }

    private void startTimeout() {
        long timeLeft = ActionPayloadSaver.getInstance(this).getTimeLeft();
        mHandler = new Handler();
        mHandler.postDelayed(mKillActivity, timeLeft);
    }

    private void finishActivity() {
        ActionPayloadSaver saver = ActionPayloadSaver.getInstance(this);
        Bundle bundle = saver.getAwaitingAction();
        if (bundle != null) {
            bundle.putBoolean(Defs.PUSH_NOTIFICATION_EXTRA_AUTHENTICATED, true);
            Intent serviceIntent = new Intent(this, NotificationBackgroundService.class);
            serviceIntent.setAction(Defs.NOTIFICATION_ACTION_CLICK);
            serviceIntent.putExtras(bundle);
            startService(serviceIntent);
            NotificationBackgroundService.acquireWakeLockNow(this);
        }
        saver.clearAwaitingAction();
        BackgroundAuthActivity.this.finish();
    }

    private void authenticateMfa() {
        ActionPayloadSaver saver = ActionPayloadSaver.getInstance(this);
        Bundle bundle = saver.getAwaitingAction();

        if (bundle == null) {
            return;
        }

        boolean authRequired = bundle.getBoolean(Defs.PUSH_NOTIFICATION_EXTRA_AUTH_REQUIRED, true);
        if (authRequired) {
            boolean deviceHasLock = isDeviceHasLock();
            if (deviceHasLock) {
                Executor executor = Executors.newSingleThreadExecutor();
                BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, mAuthCallback);
                BiometricPrompt.PromptInfo.Builder promptInfoBuilder = new BiometricPrompt.PromptInfo.Builder()
                        .setDeviceCredentialAllowed(true)
                        .setTitle(getString(R.string.biometric_title))
                        .setSubtitle(getString(R.string.biometric_subtitle))
                        .setDescription(getString(R.string.biometric_description));
                BiometricPrompt.PromptInfo promptInfo = promptInfoBuilder.build();
                biometricPrompt.authenticate(promptInfo);
            } else {
                SetLockDialog.newInstance().show(getSupportFragmentManager(), SetLockDialog.TAG);
            }
        } else {
            finishActivity();
        }
    }

    private void promptUnlock() {
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            km.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }
    }

    private void onActionAppears(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case PROMPT_UNLOCK_ACTION:
                    promptUnlock();
                    break;
                case PROMPT_BIOMETRIC_ACTION:
                    authenticateMfa();
                    break;
                default:
                    BackgroundAuthActivity.this.finish();
                    break;
            }
        } else {
            BackgroundAuthActivity.this.finish();
        }
    }

    private boolean isDeviceHasLock() {
        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                keyguardManager.isDeviceSecure() : keyguardManager.isKeyguardSecure();
    }
}