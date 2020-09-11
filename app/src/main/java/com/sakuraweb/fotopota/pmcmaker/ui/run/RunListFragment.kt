package com.sakuraweb.fotopota.pmcmaker.ui.run

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sakuraweb.fotopota.pmcmaker.MainActivity
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.trainingRealmConfig
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_run_list.*
import kotlinx.android.synthetic.main.fragment_run_list.view.*

var settingDurationSw :Boolean = true
var settingKmSw     :Boolean = true
var settingKcalSw   :Boolean = true
var settingMemoSw   :Boolean = true
var settingMenuSw   :Boolean = true
var settingPlaceSw  :Boolean = true

class TrainingListFragment : Fragment() {

    private lateinit var realm: Realm                               // とりあえず、Realmのインスタンスを作る
    private lateinit var adapter: RunRecyclerViewAdapter       // アダプタのインスタンス
    private lateinit var layoutManager: RecyclerView.LayoutManager  // レイアウトマネージャーのインスタンス


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View? {

        // このfragment自身を指す。ボタンなどを指定するには、rootが必要
        val root = inflater.inflate(R.layout.fragment_run_list, container, false)

        // ーーーーーーーーーー　表示項目のON/OFFをPreferenceから読んでおく　ーーーーーーーーーー
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            settingDurationSw= getBoolean("duration_sw", true)
            settingKmSw     = getBoolean("km_sw", true)
            settingKcalSw   = getBoolean("kcal_sw", true)
            settingMemoSw   = getBoolean("memo_sw", true)
            settingMenuSw   = getBoolean("menu_sw", true)
            settingPlaceSw  = getBoolean("place_sw", true)
        }

        // ーーーーーーーーーー　リスト表示（RecyclerView）　ーーーーーーーーーー
        // realmのインスタンスを作る。Configはグローバル化してあるので、そのままインスタンスを作るだけ
        realm = Realm.getInstance(trainingRealmConfig)

        // 追加ボタン（fab）のリスナを設定する（EditActivity画面を呼び出す）
        root.trainingFAB.setOnClickListener {
            startActivity( Intent(activity, TrainingEditActivity::class.java).apply {
                putExtra("mode", RUN_EDIT_MODE_NEW)
            })
        }

        // ーーーーーーーーーー　ツールバーやメニューの装備　ーーーーーーーーーー
        // 「戻る」ボタン
        (activity as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.title_run2)
            show()
        }

        // メニュー構築（実装はonCreateOptionsMenu内で）
        // これを呼び出すことでfragmentがメニューを持つことを明示（https://developer.android.com/guide/components/fragments?hl=ja）
//         setHasOptionsMenu(true)

        return root
    }


    // ━━━━━━━━━　いよいよここでリスト表示　━━━━━━━━━
    // RecyclerViewerのレイアウトマネージャーとアダプターを設定してあげれば、あとは自動
    override fun onStart() {
        super.onStart()

        val ma = activity as MainActivity
        val realmResults: RealmResults<RunData> = realm.where<RunData>()
            .findAll().sort("date1", Sort.DESCENDING)

        // 1行のViewを表示するレイアウトマネージャーを設定する
        // LinearLayout、GridLayout、独自も選べるが無難にLinearLayoutManagerにする
        layoutManager = LinearLayoutManager(activity)
        trainingRecylerView.layoutManager = layoutManager

        // アダプターを設定する
        adapter = RunRecyclerViewAdapter(realmResults)
        trainingRecylerView.adapter = this.adapter

    }

    // 終了処理
    // 特にきめがあって書いたわけではなく、HHCのコースセレクトダイアログに従って書いただけ見たい
    // そう考えると、書く位置を間違っているのかも
    // TODO: onStopの方がいいそうです（onDestroyは呼ばれないことがある）
    override fun onDestroy() {
        super.onDestroy()
        realm.close()

    }

}