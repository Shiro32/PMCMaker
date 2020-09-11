package com.sakuraweb.fotopota.pmcmaker.ui.pmc

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

class PmcFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_pmc, container, false)

//        root.text_notifications.text = "pmc"

        return root
    }

}

