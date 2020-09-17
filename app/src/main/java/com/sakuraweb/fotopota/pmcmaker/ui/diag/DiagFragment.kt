package com.sakuraweb.fotopota.pmcmaker.ui.diag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sakuraweb.fotopota.pmcmaker.R

class DiagFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.diagnosis_fragment, container, false)

//        root.text_notifications.text = "DIAG"

        return root
    }
}
