package com.kshitijpatil.tazabazar.ui.common

import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job

/**
 * Lifecycle Aware [Job] Manager.
 * Cancels outstanding job when lifecycle being observed reaches ON_DESTROY event.
 *
 * Optionally ([cancelOnBackPressed]) supports canceling the Job OnBackPressed
 * (Make sure you've registered the [backPressCallback] with
 * your activity dispatcher for this to work)
 * */
class LifecycleAwareJobManager(var cancelOnBackPressed: Boolean = false) :
    DefaultLifecycleObserver {
    private var job: Job? = null
    private var jobCompletionHandle: DisposableHandle? = null
    val backPressCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                job?.cancel()
            }
        }

    fun handleCancellation(creator: () -> Job) {
        backPressCallback.isEnabled = cancelOnBackPressed
        job = creator()
        jobCompletionHandle = job?.invokeOnCompletion {
            backPressCallback.isEnabled = false
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        job?.cancel()
        jobCompletionHandle?.dispose()
        backPressCallback.remove()
    }
}