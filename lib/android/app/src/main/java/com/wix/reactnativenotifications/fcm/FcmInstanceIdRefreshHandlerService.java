package com.wix.reactnativenotifications.fcm;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class FcmInstanceIdRefreshHandlerService extends JobService {

    public static final int EXTRA_IS_APP_INIT = -1;
    public static final int EXTRA_MANUAL_REFRESH = -2;

    @Override
    public boolean onStartJob(JobParameters params) {
        int jobId = params.getJobId();
        IFcmToken fcmToken = FcmToken.get(this);
        if (fcmToken == null) {
            return false;
        }

        if (jobId == EXTRA_IS_APP_INIT) {
            fcmToken.onAppReady();
        } else if (jobId == EXTRA_MANUAL_REFRESH) {
            fcmToken.onManualRefresh();
        } else {
            fcmToken.onNewTokenReady();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
