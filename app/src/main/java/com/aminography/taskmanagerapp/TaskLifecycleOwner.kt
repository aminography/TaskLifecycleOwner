package com.aminography.taskmanagerapp

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

/**
 * @author aminography
 */
class TaskLifecycleOwner private constructor(
    private val activityClass: Class<*>
) : Application.ActivityLifecycleCallbacks, LifecycleOwner {

    private var targetTaskId = -1

    private var startedCounter = 0
    private var resumedCounter = 0

    private var pauseSent = true
    private var stopSent = true

    private val handler = Handler()

    private val delayedPauseRunnable = Runnable {
        dispatchPauseIfNeeded()
        dispatchStopIfNeeded()
    }

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle = registry

    override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
        if (activityClass.isInstance(activity)) {
            targetTaskId = activity.taskId
        }
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity.taskId == targetTaskId) {
            activityStarted()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        if (activity.taskId == targetTaskId) {
            activityResumed()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity.taskId == targetTaskId) {
            activityPaused()
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity.taskId == targetTaskId) {
            activityStopped()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    private fun activityStarted() {
        startedCounter++
        if (startedCounter == 1 && stopSent) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            stopSent = false
        }
    }

    private fun activityResumed() {
        resumedCounter++
        if (resumedCounter == 1) {
            if (pauseSent) {
                registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
                pauseSent = false
            } else {
                handler.removeCallbacks(delayedPauseRunnable)
            }
        }
    }

    private fun activityPaused() {
        resumedCounter--
        if (resumedCounter == 0) {
            handler.postDelayed(delayedPauseRunnable, TIMEOUT_MS)
        }
    }

    private fun activityStopped() {
        startedCounter--
        dispatchStopIfNeeded()
    }

    private fun dispatchPauseIfNeeded() {
        if (resumedCounter == 0) {
            pauseSent = true
            registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
    }

    private fun dispatchStopIfNeeded() {
        if (startedCounter == 0 && pauseSent) {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            stopSent = true
        }
    }

    private fun attach(context: Context) {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(this)
    }

    private fun detach(context: Context) {
        (context.applicationContext as Application).unregisterActivityLifecycleCallbacks(this)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    companion object {
        private const val TIMEOUT_MS = 700L

        private var instance: TaskLifecycleOwner? = null

        fun getInstance(context: Context, activityClass: Class<*>): TaskLifecycleOwner {
            if (instance == null) {
                instance = TaskLifecycleOwner(activityClass).apply { attach(context) }
            }
            return requireNotNull(instance)
        }

        fun destroy(context: Context) {
            instance?.detach(context)
            instance = null
        }
    }
}
