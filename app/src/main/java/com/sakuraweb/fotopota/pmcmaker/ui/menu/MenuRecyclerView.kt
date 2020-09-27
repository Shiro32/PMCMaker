package com.sakuraweb.fotopota.pmcmaker.ui.menu

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.*
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.ui.run.RUN_EDIT_MODE_EDIT
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where

interface SetMenuListener {
    fun okBtnTapped( ret: MenuData? )
}

// RunEditから呼ばれたときはタップで選択
// Configから呼ばれたときはMenuEditというように動きが違うため、色々やってるみたい
class MenuRecyclerViewAdapter( menuRealm: RealmResults<MenuData>, private val listener: SetMenuListener )
    : RecyclerView.Adapter<MenuViewHolder>() {

    private val menuList: RealmResults<MenuData> = menuRealm

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        // 新しいView（1行）を生成する　レイアウト画面で作った、one_Training_card_home（1行）
        val view = LayoutInflater.from(parent.context).inflate(menuListLayout, parent, false)
        // 1行ビューをもとに、ViewHolder（←自分で作ったヤツ）インスタンスを生成
        // 今作ったView（LinearLayout）を渡す
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val menu = menuList[position]

        if (menu != null) {
            holder.name?.text = menu.name
            holder.desc?.text = menu.desc

            // 行（Card）のリスナ
            if( isCalledFromRunEdit ) {
                // RunEditから呼ばれた場合は、選択して終了
                holder.itemView.setOnClickListener {
                    listener.okBtnTapped(menu)
                }
            } else {
                // 設定画面から呼ばれた場合はEditへ（以前はポップアップだったけど）
                holder.itemView.setOnClickListener {
                    it.context.startActivity(Intent(it.context, MenuEditActivity::class.java).apply {
                        putExtra("id", menu.id)
                        putExtra("mode", RUN_EDIT_MODE_EDIT)
                    })
                }
            }
        }
    }


    // アダプターの必須の、サイズを返すメソッド
    override fun getItemCount(): Int {
        return menuList.size

    }
}


/*
    // 行タップ時のリスナ（メニュー表示）
    private inner class ItemClickListener(c: Context, m: MenuData) : View.OnClickListener {
        val ctx = c
        val mp = m

        @RequiresApi(Build.VERSION_CODES.M)
        override fun onClick(v: View?) {
            // ★ポップアップ版
            PopupMenu(ctx, v).apply {
                menuInflater.inflate(R.menu.menu_context_training, menu)
                gravity = Gravity.RIGHT
                setOnMenuItemClickListener(MenuClickListener(ctx, mp, menuRealm))
                show()
            }
        }
    }

    // ポップアップメニューの選択結果に基づいて各種処理
    private inner class MenuClickListener(c: Context, m: MenuData, realm: Realm ) : PopupMenu.OnMenuItemClickListener {
        val ctx = c
        val mp = m
        val menuRealm = realm


        override fun onMenuItemClick(item: MenuItem?): Boolean {
            when (item?.itemId) {
                // 編集メニュー（わかるとは思うけど・・・）
                R.id.ctxMenuEdit -> {
                    ctx.startActivity(Intent(ctx, MenuEditActivity::class.java).apply {
                        putExtra("id", mp.id)
                        putExtra("mode", RUN_EDIT_MODE_EDIT)
                    })
                }
                // 削除メニュー（・・・）
                R.id.ctxMenuDelete -> {
                    val builder = AlertDialog.Builder(ctx)
                    builder.setTitle(R.string.del_confirm_dialog_title)
                    builder.setMessage(R.string.del_confirm_dialog_message)
                    builder.setCancelable(true)
                    builder.setNegativeButton(R.string.del_confirm_dialog_cancel, null)
                    builder.setPositiveButton("OK", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            menuRealm.executeTransaction {
                                menuRealm.where<MenuData>().equalTo("id", mp.id)?.findFirst()
                                    ?.deleteFromRealm()
                            }
//                            blackToast(applicationContext, "削除しました")
                            menuRealm.close()
                            runListAct.recreate()

                        }
                    })
                    builder.show()
                }
            }
            return false
        }
    }
 */