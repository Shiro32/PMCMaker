package com.sakuraweb.fotopota.pmcmaker.ui.menu

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Global.getString
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.sakuraweb.fotopota.pmcmaker.*
import com.sakuraweb.fotopota.pmcmaker.ui.run.*
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.menu_edit_activity.*
import kotlinx.android.synthetic.main.run_edit_activity.*

const val MENU_EDIT_MODE_NEW = 1
const val MENU_EDIT_MODE_EDIT = 2

class MenuEditActivity : AppCompatActivity() {
    private var editMode: Int = 0
    private var menuID: Long = 0
    private lateinit var realm: Realm
    private lateinit var inputMethodManager: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_edit_activity)

        // 背景タッチ確定用
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // ツールバー関係
        setSupportActionBar(menuEditToolbar) // これやらないと落ちるよ

        // Realmのインスタンスを生成
        // Edit画面終了まで維持（onDestroy）でclose
        // configはStartActivityで生成済み
        realm = Realm.getInstance(menuRealmConfig)

        // 呼び出し元から、どのような動作を求められているか（新規、編集）
        // 新規はFABボタン（MENU_EDIT_MODE_NEW）
        // 編集はポップアップメニュー（MENU_EDIT_MODE_EDIT）
        editMode = intent.getIntExtra("mode", MENU_EDIT_MODE_NEW)

        // 呼び出しのMENU-LISTの、どこから呼ばれたのか（Realm上のID）
        menuID = intent.getLongExtra("id", 0L)

        // 既存データをRealmDBからダイアログのViewに読み込む
        when (editMode) {
            MENU_EDIT_MODE_NEW -> {
                supportActionBar?.title = getString(R.string.title_menu_new)
                menuEditDeleteBtn.visibility = View.GONE
            }

            MENU_EDIT_MODE_EDIT -> {
                supportActionBar?.title = getString(R.string.title_menu_edit)

                val m = realm.where<MenuData>().equalTo("id", menuID).findFirst()
                if (m != null) {
                    menuEditNameEdit.setText(m.name)
                    menuEditDescEdit.setText(m.desc)

                    // 編集画面の時のみ、削除ボタンを作る
                    menuEditDeleteBtn.setOnClickListener {
                        AlertDialog.Builder(this).apply {
                            setTitle(R.string.del_confirm_dialog_title)
                            setMessage(R.string.del_confirm_dialog_message)
                            setCancelable(true)
                            setNegativeButton(R.string.del_confirm_dialog_cancel, null)
                            setPositiveButton("OK",
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        realm.executeTransaction { realm.where<MenuData>().equalTo("id", m.id)?.findFirst()?.deleteFromRealm() }
                                        blackToast(applicationContext, "削除しました")
                                        finish()
                                    }
                                })
                            show()
                        }
                    }
                }
            }
        } // when(editMode )

        // ーーーーーーーーーー　ここから各種ボタンのリスナ群設定　－－－－－－－－－－
        // SAVEボタンのリスナ。デカいので外だし
        menuEditSaveBtn.setOnClickListener(OKButtonListener())

        // キャンセルボタン
        menuEditCancelBtn.setOnClickListener { finish() }

        // 戻るボタン。表示だけで、実走はonSupportNavigateUp()で。超面倒くせえ！
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    // ツールバーの「戻る」ボタン
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    // OKButton（保存）のリスナがあまりに巨大化してきたので独立
    // RealmDBに１件分のBREWデータを修正・追加する （intentのmodeによって、編集と新規作成両方やる）
    private inner class OKButtonListener() : View.OnClickListener {
        override fun onClick(v: View?) {
            // ゼロチェックしないといけないんだってさ！！
            val name = menuEditNameEdit.text.toString()
            val desc = menuEditDescEdit.text.toString()

            when (editMode) {
                MENU_EDIT_MODE_NEW -> {
                    realm.executeTransaction {
                        // whereで最後尾を探し、そこに追記
                        val maxID = realm.where<MenuData>().max("id")
                        val nextID = (maxID?.toLong() ?: 0L) + 1L

                        // ここから書き込み
                        val m = realm.createObject<MenuData>(nextID)
                        m.name = name
                        m.desc = desc
                    }
                }
                MENU_EDIT_MODE_EDIT -> {
                    realm.executeTransaction {
                        val m = realm.where<MenuData>().equalTo("id", menuID).findFirst()
                        m?.name = name
                        m?.desc = desc
                    }
                }
            } // editMode

            // これで編集作業完了！
            finish()
        } // onClick
    } // OKListener

    // 入力箇所（EditText）以外をタップしたときに、フォーカスをオフにする
    // おおもとのLayoutにfocusableInTouchModeをtrueにしないといけない
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        // キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(menuEditLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
        // 背景にフォーカスを移す
        menuEditLayout.requestFocus()
        return super.dispatchTouchEvent(event)
    }
}

fun findMenuNameByID( ctx: Context, menuID: Long ): String {
    val realm = Realm.getInstance(menuRealmConfig)
    val menu = realm.where<MenuData>().equalTo("id",menuID).findFirst()
    var name = menu?.name.toString()
    realm.close()
    if (name != "null") return name else return ctx.getString(R.string.run_menu_unselected)
}

