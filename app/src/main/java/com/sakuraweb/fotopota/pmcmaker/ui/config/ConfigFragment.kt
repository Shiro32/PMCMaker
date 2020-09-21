package com.sakuraweb.fotopota.pmcmaker.ui.config

import android.content.Intent
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.ui.menu.MenuListActivity
import kotlinx.android.synthetic.main.home_fragment.view.*

// TODO: EDIT入力を数字限定にできないのか？ 落ちそう
// TODO: 入力画面にOKやSAVEが無いので、チェックするタイミングすらないぞ？


class ConfigFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.root_preferences)

//        root.setMenuBtn.setOnClickListener {
//            val intent = Intent(activity, MenuListActivity::class.java)
//            startActivity(intent)
//        }
    }
}
