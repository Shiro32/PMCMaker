package com.sakuraweb.fotopota.pmcmaker.ui.home

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sakuraweb.fotopota.pmcmaker.*
import io.realm.Realm
import kotlinx.android.synthetic.main.home_fragment.view.*
import java.io.File



class HomeFragment : androidx.fragment.app.Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val root = inflater.inflate(R.layout.home_fragment, container, false)

//        root.setMenuBtn.setOnClickListener {
//            val intent = Intent(activity, MenuListActivity::class.java)
//            startActivity(intent)
//        }

        // テスト用に置いたボタン（本来なら設定画面に作りましょう）
/*
        root.mainBackupBtn.setOnClickListener {
            // ランリスト
            var realm = Realm.getInstance(runRealmConfig)
            var src = File(realm.path)
            var dst = File(context?.getExternalFilesDir(null).toString() + "/" + run_list_backup)
            src.copyTo(dst, overwrite = true)
            realm.close()

            // トレーニングメニューリスト
            realm = Realm.getInstance(menuRealmConfig)
            src = File(realm.path)
            dst = File(context?.getExternalFilesDir(null).toString() + "/" + menu_list_backup)
            src.copyTo(dst, overwrite = true)
            realm.close()

            blackToast(context as Context, "バックアップ完了！")
        }
*/


        // copyrightメッセージにURLを埋め込む
        root.copyRightText.setText(Html.fromHtml("v2.0 Copyright ©2020 Shiro, <a href=\"http://fotopota.sakuraweb.com\">フォトポタ日記2.0</a>"))
        root.copyRightText.movementMethod = LinkMovementMethod.getInstance()

        // privacy policyにURLを埋め込む
        root.ppText.setText(Html.fromHtml("<a href=\"http://fotopota.sakuraweb.com/privacy-pmc.html\">プライバシーポリシー</a>"))
        root.ppText.movementMethod = LinkMovementMethod.getInstance()

        return root
    }
}