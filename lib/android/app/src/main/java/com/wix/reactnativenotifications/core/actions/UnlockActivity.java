package com.wix.reactnativenotifications.core.actions;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.wix.reactnativenotifications.Defs;
import com.wix.reactnativenotifications.core.NotificationBackgroundService;

public class UnlockActivity extends AppCompatActivity {

    public static final String PROMPT_UNLOCK_ACTION = "prompt_unlock";

    private Handler mHandler;
    private Runnable mKillActivity = new Runnable() {
        @Override
        public void run() {
            moveTaskToBack(true);
            finish();
        }
    };

    private BroadcastReceiver mUnlockReceiver = new BroadcastReceiver() {
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
                finish();
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

    private void onActionAppears(Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(PROMPT_UNLOCK_ACTION)) {
            KeyguardManager km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                km.requestDismissKeyguard(this, null);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        } else {
            finish();
        }
        moveTaskToBack(true);
    }
}