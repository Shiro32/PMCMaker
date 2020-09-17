package com.sakuraweb.fotopota.pmcmaker.ui.menu

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.menuRealmConfig
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.menu_list_activity.*

// TODO: ポップアップメニューやめようかな。List→Editに遷移して、キャンセル・削除・保存の３ボタンで

var isCalledFromRunEdit: Boolean = false

class MenuListActivity : AppCompatActivity(), SetMenuListener {

    private lateinit var realm: Realm                               // とりあえず、Realmのインスタンスを作る
    private lateinit var adapter: MenuRecyclerViewAdapter       // アダプタのインスタンス
    private lateinit var layoutManager: RecyclerView.LayoutManager  // レイアウトマネージャーのインスタンス

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // レイアウトインフレート
        setContentView(R.layout.menu_list_activity)

        // Runの編集画面から呼ばれたかどうかを覚えておく
        isCalledFromRunEdit = intent.getStringExtra("from") == "Run"

        // ーーーーーーーーーー　リスト表示（RecyclerView）　ーーーーーーーーーー
        // realmのインスタンスを作る。Configはグローバル化してあるので、そのままインスタンスを作るだけ
        realm = Realm.getInstance(menuRealmConfig)

        // 追加ボタン（fab）のリスナを設定する（EditActivity画面を呼び出す）
        // 最初はRunEditでは書かないことにしたけど、やっぱりつけよう。あったっていいじゃん
        menuFAB.setOnClickListener {
            startActivity(Intent(applicationContext, MenuEditActivity::class.java).apply {
                putExtra("mode", MENU_EDIT_MODE_NEW)
            })
        }

        // ーーーーーーーーーー　ツールバーやメニューの装備　ーーーーーーーーーー
        setSupportActionBar(menuListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)   // 実装は自分でする必要あるので注意
        supportActionBar?.setDisplayShowHomeEnabled(true)   // 実装は自分でする必要あるので注意

        supportActionBar?.title =
            getString( if(isCalledFromRunEdit) R.string.title_menu_from_run_edit else R.string.title_menu_from_config)
    }

    // ツールバーの「戻る」ボタン
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    // ━━━━━━━━━　いよいよここでリスト表示　━━━━━━━━━
    // RecyclerViewerのレイアウトマネージャーとアダプターを設定してあげれば、あとは自動
    override fun onStart() {
        super.onStart()

        val realmResults: RealmResults<MenuData> = realm.where<MenuData>()
            .findAll().sort("name", Sort.DESCENDING)

        // 1行のViewを表示するレイアウトマネージャーを設定する
        // LinearLayout、GridLayout、独自も選べるが無難にLinearLayoutManagerにする
        layoutManager = LinearLayoutManager(applicationContext)
        menuRecylerView.layoutManager = layoutManager

        // アダプターを設定する
        adapter = MenuRecyclerViewAdapter(realmResults, realm, this, this)
        menuRecylerView.adapter = this.adapter

    }

    override fun okBtnTapped(ret: MenuData?) {
        val intent = Intent()
        intent.putExtra("id", ret?.id )
        intent.putExtra( "name", ret?.name )

        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}