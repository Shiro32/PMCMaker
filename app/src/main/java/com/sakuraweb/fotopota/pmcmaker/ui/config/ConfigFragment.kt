package com.sakuraweb.fotopota.pmcmaker.ui.config

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.sakuraweb.fotopota.pmcmaker.R

/*
class SettingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.fragment_setting, container, false)

//        root.text_notifications.text = "Setting"

        return root
    }
}
*/


class ConfigFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        setPreferencesFromResource(R.xml.root_preferences, rootKey)

    }
}


