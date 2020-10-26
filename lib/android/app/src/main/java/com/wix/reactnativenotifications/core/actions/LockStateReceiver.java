package com.wix.reactnativenotifications.core.actions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.KeyguardManager;
import android.os.Bundle;

public class LockStateReceiver extends BroadcastReceiver {

    private void sendAwaitingAction(Context context) {
        ActionPayloadSaver saver = ActionPayloadSaver.getInstance(context);
        Bundle bundle = saver.getAwaitingAction();
        if (bundle != null) {
            Intent intent = new Intent(context, NotificationActionService.class);
            intent.putExtras(bundle);
            context.startService(intent);
            NotificationActionService.acquireWakeLockNow(context);
        }
        saver.clearAwaitingAction();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (action != null && !myKM.inKeyguardRestrictedInputMode()) {
            sendAwaitingAction(context);
        }
    }

}
