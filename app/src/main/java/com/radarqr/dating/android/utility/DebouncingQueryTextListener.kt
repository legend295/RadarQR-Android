package com.radarqr.dating.android.utility

import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class DebouncingQueryTextListener(
    lifecycle: Lifecycle,
    private val onDebouncingQueryTextChange: (String?) -> Unit
) : TextWatcher {
    var debouncePeriod: Long = 500

    private val coroutineScope = lifecycle.coroutineScope

    private var searchJob: Job? = null

    /*override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            newText?.let {
                delay(debouncePeriod)
                onDebouncingQueryTextChange(newText)
            }
        }
        return false
    }
*/
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            s?.let {
                delay(debouncePeriod)
                onDebouncingQueryTextChange(s.toString())
            }
        }
    }

    override fun afterTextChanged(s: Editable?) {

    }
}