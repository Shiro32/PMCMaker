package com.sakuraweb.fotopota.pmcmaker.ui.run

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.*
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.placeList
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.one_run_card.view.*
import java.text.SimpleDateFormat


class RunRecyclerViewAdapter(trainingRealm: RealmResults<RunData>, realm: Realm, fmt: RunListFragment ) :
        RecyclerView.Adapter<RunViewHolder>() {

    private val trainingList: RealmResults<RunData> = trainingRealm
    private val runRealm = realm
    private val runlistFmt = fmt

    // 新しく1行分のViewをXMLから生成し、1行分のViewHolderを生成してViewをセットする
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        // 新しいView（1行）を生成する　レイアウト画面で作った、one_Training_card_home（1行）
        val view = LayoutInflater.from(parent.context).inflate(R.layout.one_run_card, parent, false)
        if( !settingTermSw ) {
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
            holder.dateText?.text   = SimpleDateFormat("yyyy/MM/dd").format(training.date)
            holder.termText?.text   = SimpleDateFormat( "HH時間mm分").format(training.term)
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
            // TODO: 本当はロングタップでやりたいんだけど分からず・・・
            holder.itemView.setOnClickListener(ItemClickListener(holder.itemView.context, training ))

        }

    } // override onBindViewHolder


    // 行タップ時のリスナ（メニュー表示）
    private inner class ItemClickListener(c: Context, r: RunData) : View.OnClickListener {
        // こうやって独自の変数を渡せばいいんだ！　←　いや親クラス内でローカルにしておけば継承されますが・・・
        // 独自クラスのコンストラクタに設定しておいて、クラス内ローカル変数に保存しておく
        // こうすれば、クラス内のメソッドから参照できる
        val ctx = c
        val rp = r

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onClick(v: View?) {
            // ここで使えるのはタップされたView（１行レイアウト、one_brew_card_home）

            // ★ポップアップ版
            PopupMenu(ctx, v).apply {
                menuInflater.inflate(R.menu.menu_context_training, menu)
                gravity = Gravity.RIGHT
                setOnMenuItemClickListener(MenuClickListener(ctx, rp, runRealm ))
                show()
            }
        }
    }

    // ポップアップメニューの選択結果に基づいて各種処理
    private inner class MenuClickListener(c: Context, r: RunData, realm: Realm ) : PopupMenu.OnMenuItemClickListener {
        val ctx = c
        val rp = r
        val runRealm = realm


        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when( item?.itemId ) {
                // 編集メニュー（わかるとは思うけど・・・）
                R.id.ctxMenuTrainingEdit -> {
                    ctx.startActivity( Intent(ctx, TrainingEditActivity::class.java).apply {
                        putExtra("id", rp.id )
                        putExtra( "mode", RUN_EDIT_MODE_EDIT )
                    })
                }
                // 削除メニュー（・・・）
                R.id.ctxMenuTrainingDelete -> {
                    val builder = AlertDialog.Builder( ctx )
                    builder.setTitle(R.string.del_confirm_dialog_title)
                    builder.setMessage(R.string.del_confirm_dialog_message)
                    builder.setCancelable(true)
                    builder.setNegativeButton(R.string.del_confirm_dialog_cancel, null)
                    builder.setPositiveButton("OK", object: DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            runRealm.executeTransaction { runRealm.where<RunData>().equalTo("id", rp.id)?.findFirst()?.deleteFromRealm() }
//                            blackToast(applicationContext, "削除しました")
                            // ここで、RecyclerViewをReDrawしてやるのだけど、どうやって・・・？
                            // notifyItemRemoved(which)
//                            runRealm.close()
                            runlistFmt.onStart()
                        }
                    })
                    builder.show()
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
