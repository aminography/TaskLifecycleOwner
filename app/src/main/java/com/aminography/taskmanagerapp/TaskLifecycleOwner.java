package com.aminography.taskmanagerapp;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

/**
 * @author aminography
 */
class TaskLifecycleOwner implements Application.ActivityLifecycleCallbacks, LifecycleOwner {

    private final Class<?> activityClass;
    private int targetTaskId = -1;

    private static final long TIMEOUT_MS = 700;

    // ground truth counters
    private int mStartedCounter = 0;
    private int mResumedCounter = 0;

    private boolean mPauseSent = true;
    private boolean mStopSent = true;

    private final Handler mHandler = new Handler();
    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

    // To distinguish between screen rotation and normal lifecycle changes:
    private final Runnable mDelayedPauseRunnable = new Runnable() {
        @Override
        public void run() {
            dispatchPauseIfNeeded();
            dispatchStopIfNeeded();
        }
    };

    private static TaskLifecycleOwner sInstance = null;

    public static TaskLifecycleOwner getInstance(Context context, Class<?> activityClass) {
        if (sInstance == null) {
            sInstance = new TaskLifecycleOwner(activityClass);
            sInstance.attach(context);
        }
        return sInstance;
    }

    public static void destroyInstance(Context context) {
        if (sInstance != null) {
            sInstance.detach(context);
            sInstance = null;
        }
    }

    private TaskLifecycleOwner(Class<?> activityClass) {
        this.activityClass = activityClass;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
        if (activityClass.isInstance(activity)) {
            targetTaskId = activity.getTaskId();
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (activity.getTaskId() == targetTaskId) {
            activityStarted();
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (activity.getTaskId() == targetTaskId) {
            activityResumed();
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (activity.getTaskId() == targetTaskId) {
            activityPaused();
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (activity.getTaskId() == targetTaskId) {
            activityStopped();
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mRegistry;
    }

    private void activityStarted() {
        mStartedCounter++;
        if (mStartedCounter == 1 && mStopSent) {
            mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
            mStopSent = false;
        }
    }

    private void activityResumed() {
        mResumedCounter++;
        if (mResumedCounter == 1) {
            if (mPauseSent) {
                mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
                mPauseSent = false;
            } else {
                mHandler.removeCallbacks(mDelayedPauseRunnable);
            }
        }
    }

    private void activityPaused() {
        mResumedCounter--;
        if (mResumedCounter == 0) {
            mHandler.postDelayed(mDelayedPauseRunnable, TIMEOUT_MS);
        }
    }

    private void activityStopped() {
        mStartedCounter--;
        dispatchStopIfNeeded();
    }

    private void dispatchPauseIfNeeded() {
        if (mResumedCounter == 0) {
            mPauseSent = true;
            mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
        }
    }

    private void dispatchStopIfNeeded() {
        if (mStartedCounter == 0 && mPauseSent) {
            mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
            mStopSent = true;
        }
    }

    private void attach(Context context) {
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(this);
    }

    private void detach(Context context) {
        ((Application) context.getApplicationContext()).unregisterActivityLifecycleCallbacks(this);
        mRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }
}
