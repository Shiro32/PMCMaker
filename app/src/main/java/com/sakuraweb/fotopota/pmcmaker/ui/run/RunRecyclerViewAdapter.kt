package com.sakuraweb.fotopota.pmcmaker.ui.run

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.*
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.placeList
import io.realm.RealmResults
import kotlinx.android.synthetic.main.one_run_card.view.*
import java.text.SimpleDateFormat


class RunRecyclerViewAdapter(trainingRealm: RealmResults<RunData> ) :
        RecyclerView.Adapter<RunViewHolder>() {

    private val trainingList: RealmResults<RunData> = trainingRealm

    // 新しく1行分のViewをXMLから生成し、1行分のViewHolderを生成してViewをセットする
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        // 新しいView（1行）を生成する　レイアウト画面で作った、one_Training_card_home（1行）
        val view = LayoutInflater.from(parent.context).inflate(R.layout.one_run_card, parent, false)
        if( !settingDurationSw ) {
            view.oneRunDuration.visibility = View.GONE
        }
        if( !settingKmSw ) {
            view.oneRunKm.visibility = View.GONE
            view.oneRunKmLabel.visibility = View.GONE
        }
        if( !settingKcalSw ) {
            view.oneRunKcal.visibility = View.GONE
            view.oneRunKcalLabel.visibility = View.GONE
        }
        if( !settingMemoSw ) {
            view.oneRunMemo.visibility = View.GONE
        }
        if( !settingPlaceSw ) {
            view.oneRunPlace.visibility = View.GONE
        }
        if( !settingMenuSw ) {
            view.oneRunMenu.visibility = View.GONE
        }

        // 1行ビューをもとに、ViewHolder（←自分で作ったヤツ）インスタンスを生成
        // 今作ったView（LinearLayout）を渡す
        return RunViewHolder(view)
    }



    // ViewHolderの表示内容を更新する。RecyclerViewの心臓部
    // 渡されたビューホルダにデータを書き込む
    // RealmDB内のデータから、具体的なビューの表示文字列を生成してあげる
    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val training = trainingList[position]

        if (training != null) {
            holder.dateText?.text = SimpleDateFormat("yyyy/MM/dd").format(training.date1)
            holder.durationText?.text = SimpleDateFormat( "HH時間mm分").format(training.date2)
            holder.tssText?.text    = training.tss.toString()
            holder.kmText?.text     = training.km.toString()
            holder.kcalText?.text   = training.kcal.toString()
            holder.placeText?.text  = placeList[training.place]
            holder.memoText?.text   = training.memo

/*
            // 行タップした際のアクションをリスナで登録
            // ボタンは廃止しました
            if (isCalledFromBrewEditToTraining) {
                // Brew-Editから呼び出された場合は、豆を選択なのでタップで決定とする
                holder.itemView.setOnClickListener {
                    listener.okBtnTapped(training)
                }
            } else {
                // Naviから呼び出された場合は、豆を編集する
                holder.itemView.setOnClickListener {
                    val intent = Intent(it.context, TrainingDetailsActivity::class.java)
                    intent.putExtra("id", training.id)
                    val it2 = it.context as Activity
                    it2.startActivityForResult(intent, REQUEST_CODE_SHOW_Training_DETAILS)
//                    it.context.startActivity(intent)
                }
            }
*/
            // 行そのもの（Card）のリスナ
            // 行タップすることで編集画面(BrewEdit）に移行
            // 戻り値によって、TO_LISTやTO_HOMEもあり得るのでforResultで呼ぶ
            holder.itemView.setOnClickListener(ItemClickListener(holder.itemView.context, training))

        }

    } // override onBindViewHolder


    private inner class ItemClickListener(c: Context, b: RunData) : View.OnClickListener {
        // こうやって独自の変数を渡せばいいんだ！　←　いや親クラス内でローカルにしておけば継承されますが・・・
        // 独自クラスのコンストラクタに設定しておいて、クラス内ローカル変数に保存しておく
        // こうすれば、クラス内のメソッドから参照できる
        val ctx = c
        val ctx2 = ctx as Activity
        val bp = b

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onClick(v: View?) {
            // ここで使えるのはタップされたView（１行レイアウト、one_brew_card_home）

            // ★ポップアップ版
            val popup = PopupMenu(ctx, v)
            popup.menuInflater.inflate(R.menu.menu_context_training, popup.menu)
            popup.gravity = Gravity.CENTER
            popup.setOnMenuItemClickListener(MenuClickListener(ctx, bp))
            popup.show()

////            ★とりあえずDetailsへ
//            val intent = Intent(ctx, BrewDetailsActivity::class.java)
//            intent.putExtra("id", bp.id)
//            ctx2.startActivityForResult(intent, REQUEST_CODE_SHOW_DETAILS)
        }
    }


    // ポップアップメニューの選択結果に基づいて各種処理
    // 直接ボタンを置くのとどちらがイイか悩ましいけど、とりあえずポップアップ式で
    private inner class MenuClickListener(c: Context, t: RunData) : PopupMenu.OnMenuItemClickListener {
        val ctx = c
        val tp = t

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when( item?.itemId ) {
                R.id.ctxMenuTrainingEdit -> {
                    ctx.startActivity( Intent(ctx, TrainingEditActivity::class.java).apply {
                        putExtra("id", tp.id )
                        putExtra( "mode", RUN_EDIT_MODE_EDIT )
                    })
                }
                R.id.ctxMenuTrainingDelete -> {
//                    val ctx2 = ctx as Activity
//                    val intent = Intent(ctx, BrewEditActivity::class.java)
//                    intent.putExtra("id", bp.id)
//                    ctx2.startActivityForResult(intent, REQUEST_CODE_SHOW_DETAILS)
                }
            }

            return false
        }
    }


    // アダプターの必須昨日の、サイズを返すメソッド
    override fun getItemCount(): Int {
        return trainingList.size

    }
}
