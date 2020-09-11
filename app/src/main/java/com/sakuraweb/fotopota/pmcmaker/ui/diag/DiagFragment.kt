package com.sakuraweb.fotopota.pmcmaker.ui.diag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.sakuraweb.fotopota.pmcmaker.R
import kotlinx.android.synthetic.main.fragment_notifications.view.*

class DiagFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_diagnosis, container, false)

//        root.text_notifications.text = "DIAG"

        return root
    }
}