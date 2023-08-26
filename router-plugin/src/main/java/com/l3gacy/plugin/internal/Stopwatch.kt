package com.l3gacy.plugin.internal

import java.util.concurrent.TimeUnit

class Stopwatch {

    private var start: Long = -1L
    private var lastSplit: Long = -1L
    private lateinit var label: String

    /**
     * Start the stopwatch.
     */
    fun start(label: String) {
        check(start == -1L) {
            "Stopwatch was already started"
        }
        this.label = label
        start = System.nanoTime()
        lastSplit = start
    }

    /**
     * Reports the split time.
     *
     * @param label Label to use when printing split time
     * @param reportDiffFromLastSplit if `true` report the time from last split instead of the start
     */
    fun splitTime(label: String, reportDiffFromLastSplit: Boolean = true) {
        val split = System.nanoTime()
        val diff = if (reportDiffFromLastSplit) { split - lastSplit } else { split - start }
        lastSplit = split
        Log.v("$label: ${TimeUnit.NANOSECONDS.toMillis(diff)} ms.")
    }

    /**
     * Stops the timer and report the result.
     */
    fun stop() {
        val stop = System.nanoTime()
        val diff = stop - start
        Log.v("$label: ${TimeUnit.NANOSECONDS.toMillis(diff)} ms.")
    }
}
