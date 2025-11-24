package com.example.thread

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class ProgressFragment : Fragment(R.layout.frag_progress) {
    private val vm: WorkViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val bar = view.findViewById<com.google.android.material.progressindicator.CircularProgressIndicator>(R.id.progressBar)
        val text = view.findViewById<TextView>(R.id.txt)
        bar.isIndeterminate = true  // placeholder to keep indeterminate default
        vm.status.observe(viewLifecycleOwner) { s ->
            text.text = s
            if (s.startsWith("Working")) bar.isIndeterminate = false
        }
        vm.progress.observe(viewLifecycleOwner) { p ->
            if (!bar.isIndeterminate) {
                bar.max = 100
                bar.progress = p
                text.text = getString(R.string.status_working_format, p)
            }
        }
    }
}