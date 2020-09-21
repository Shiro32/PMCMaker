package com.sakuraweb.fotopota.pmcmaker.ui.run

import android.content.Intent
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.placeList
import com.sakuraweb.fotopota.pmcmaker.ui.menu.findMenuNameByID
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.run_one_flat.view.*
import java.text.SimpleDateFormat

class RunRecyclerViewAdapter(runRealm: RealmResults<RunData>, realm: Realm, fmt: RunListFragment ) :
        RecyclerView.Adapter<RunViewHolder>() {
    private val runList: RealmResults<RunData> = runRealm
    private val runRealm = realm
    private val runListFmt = fmt

    // 新しく1行分のViewをXMLから生成し、1行分のViewHolderを生成してViewをセットする
    // 新しいView（1行）を生成する　レイアウト画面で作った、one_Training_card_home（1行）
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val view = LayoutInflater.from(parent.context).inflate( listLayout, parent, false)
        if( !settingTermSw ) view.oneRunDuration.visibility = View.GONE
        if( !settingMemoSw ) view.oneRunMemo.visibility = View.GONE
        if( !settingPlaceSw ) view.oneRunPlace.visibility = View.GONE
        if( !settingMenuSw ) view.oneRunMenu.visibility = View.GONE
        if( !settingKmSw ) {
            view.oneRunKm.visibility = View.GONE
            view.oneRunKmLabel.visibility = View.GONE
        }
        if( !settingKcalSw ) {
            view.oneRunKcal.visibility = View.GONE
            view.oneRunKcalLabel.visibility = View.GONE
        }
        return RunViewHolder(view)
    }

    // ViewHolderの表示内容を更新する。RecyclerViewの心臓部
    // 渡されたビューホルダにデータを書き込む RealmDB内のデータから、具体的なビューの表示文字列を生成してあげる
    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = runList[position]

        if (run != null) {
            holder.dateText?.text   = SimpleDateFormat("yyyy/MM/dd").format(run.date)
            holder.termText?.text   = SimpleDateFormat( holder.itemView.context.getString(R.string.one_run_time_format)).format(run.term)
            holder.tssText?.text    = run.tss.toString()
            holder.kmText?.text     = run.km.toString()
            holder.kcalText?.text   = run.kcal.toString()
            holder.placeText?.text  = placeList[run.place]
            holder.memoText?.text   = run.memo
            holder.menuText?.text   = findMenuNameByID( holder.itemView.context, run.menuID )

            // 行そのもの（Card）のリスナ
            holder.itemView.setOnClickListener {
                it.context.startActivity( Intent(it.context, RunEditActivity::class.java).apply {
                    putExtra("id", run.id )
                    putExtra( "mode", RUN_EDIT_MODE_EDIT )
                })
            }
        }
    }

    // アダプターの必須の、サイズを返すメソッド
    override fun getItemCount(): Int {
        return runList.size

    }
}
