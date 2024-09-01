package com.example.util.simpletimetracker.core.dialog

interface DurationDialogListener {

    fun onDurationSet(durationSeconds: Long, tag: String? = null) {}

    fun onCountSet(count: Long, tag: String? = null) {}

    fun onDisable(tag: String?) {}
}