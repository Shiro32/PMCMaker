package com.sakuraweb.fotopota.pmcmaker.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.ui.menu.MenuEditActivity
import com.sakuraweb.fotopota.pmcmaker.ui.menu.MenuListActivity
import com.sakuraweb.fotopota.pmcmaker.ui.run.REQUEST_CODE_MENU_SELECT
import kotlinx.android.synthetic.main.home_fragment.view.*

class HomeFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.home_fragment, container, false)

        root.setMenuBtn.setOnClickListener {
            val intent = Intent(activity, MenuListActivity::class.java)
            startActivity(intent)
        }

        return root
    }
}