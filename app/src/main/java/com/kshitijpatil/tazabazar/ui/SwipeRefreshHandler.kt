package com.kshitijpatil.tazabazar.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Lifecycle aware Swipe Refresh Handler. Launches refresh action
 * in provided [scope], cancels the job alongside setting [swipeRefreshLayout.isRefreshing]=false
 * when lifecycle being observed reaches `onStop`
 */
class SwipeRefreshHandler(
    private val scope: CoroutineScope,
    swipeRefreshLayout: SwipeRefreshLayout,
    private val action: suspend () -> Unit
) : SwipeRefreshLayout.OnRefreshListener, DefaultLifecycleObserver {
    private val layoutRef = WeakReference(swipeRefreshLayout)
    private var refreshJob: Job? = null
    override fun onRefresh() {
        refreshJob = scope.launch { action() }
        refreshJob?.invokeOnCompletion {
            layoutRef.get()?.isRefreshing = false
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        refreshJob?.cancel()
        layoutRef.get()?.isRefreshing = false
    }
}