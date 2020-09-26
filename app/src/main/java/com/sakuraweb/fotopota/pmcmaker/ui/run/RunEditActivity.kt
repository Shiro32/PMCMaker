package com.sakuraweb.fotopota.pmcmaker.ui.run

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.blackToast
import com.sakuraweb.fotopota.pmcmaker.runRealmConfig
import com.sakuraweb.fotopota.pmcmaker.toDate
import com.sakuraweb.fotopota.pmcmaker.ui.menu.MenuListActivity
import com.sakuraweb.fotopota.pmcmaker.ui.menu.findMenuNameByID
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.run_edit_activity.*
import java.util.*

// TODO: 日付データのゼロサプレスをちゃんとやっちゃおう

// この画面の呼び出しモード
const val RUN_EDIT_MODE_NEW = 1
const val RUN_EDIT_MODE_EDIT = 2

const val REQUEST_CODE_MENU_SELECT = 1

// Trainingの各カードの編集画面
// 全画面表示のダイアログ
// 呼び出し元は、ListのEdit（編集） or FAB（新規）
// Edit - 当該データのRealm IDをIntentで送ってくる
// FAB  - もちろん何もない

class RunEditActivity : AppCompatActivity() {
    private var editMode: Int = 0
    private var runID: Long = 0
    private var menuID: Long = 0
    private lateinit var realm: Realm
    private lateinit var inputMethodManager: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.run_edit_activity)

        // 背景タッチ確定用
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        // ツールバー関係
        setSupportActionBar(runEditToolbar) // これやらないと落ちるよ

        // ーーーーーーーーーー　表示項目のON/OFFをPreferenceから読んでおく　ーーーーーーーーーー
        PreferenceManager.getDefaultSharedPreferences(applicationContext).apply {
            settingTermSw   = getBoolean("duration_sw", true)
            settingKmSw     = getBoolean("km_sw", true)
            settingKcalSw   = getBoolean("kcal_sw", true)
            settingMemoSw   = getBoolean("memo_sw", true)
            settingMenuSw   = getBoolean("menu_sw", true)
            settingPlaceSw  = getBoolean("place_sw", true)
        }
        if( !settingTermSw ) {
            runEditDurationText.visibility = View.GONE
            runEditDurationLabel.visibility = View.GONE
        }
        if( !settingKmSw ) {
            runEditKmEdit.visibility = View.GONE
            runEditKmLabel.visibility = View.GONE
            runEditKmUnitLabel.visibility = View.GONE
        }
        if( !settingKcalSw ) {
            runEditKcalEdit.visibility = View.GONE
            runEditKcalLabel.visibility = View.GONE
            runEditKcalUnitLabel.visibility = View.GONE
        }
        if( !settingMemoSw ) {
            runEditMemoEdit.visibility = View.GONE
            runEditMemoLabel.visibility = View.GONE
        }
        if( !settingPlaceSw ) {
            runEditInOutSwLabel.visibility = View.GONE
            runEditPlaceSw.visibility = View.GONE
            runEditOutdoorLabel.visibility = View.GONE
            runEditIndoorLabel.visibility = View.GONE
        }
        if( !settingMenuSw ) {
            runEditMenuLabel.visibility = View.GONE
            runEditMenuText.visibility = View.GONE
        }

        // Realmのインスタンスを生成
        // Edit画面終了まで維持（onDestroy）でclose
        realm = Realm.getInstance(runRealmConfig)

        // 呼び出し元から、どのような動作を求められているか（新規、編集）
        // 新規はFABボタン（TRAINING_EDIT_MODE_NEW）
        // 編集はポップアップメニュー（TRAINING_EDIT_MODE_EDIT）
        editMode = intent.getIntExtra("mode", RUN_EDIT_MODE_NEW)

        // 呼び出しのTRAINING-LISTの、どこから呼ばれたのか（Realm上のID）
        runID = intent.getLongExtra("id", 0L)

        // 入力ダイアログ用に現在日時を取得しておく（インスタンス化と現在日時同時）
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        
        // 既存データをRealmDBからダイアログのViewに読み込む
        when( editMode ) {
            RUN_EDIT_MODE_NEW -> {
                supportActionBar?.title = getString(R.string.title_run_new)
                runEditDeleteBtn.visibility = View.GONE

                // cal2（継続時間）を30分にリセット。cal1は本日の日付でＯＫ
                cal2.set( Calendar.HOUR_OF_DAY, 0)
                cal2.set( Calendar.MINUTE, 30)
                cal2.set( Calendar.SECOND, 0)
            }

            RUN_EDIT_MODE_EDIT -> {
                supportActionBar?.title = getString(R.string.title_run_edit)

                val r = realm.where<RunData>().equalTo("id", runID).findFirst()
                if (r != null) {
                    runEditTssEdit  .setText(r.tss.toString())
                    runEditKmEdit   .setText(r.km.toString())
                    runEditKcalEdit .setText(r.kcal.toString())
                    runEditMemoEdit .setText(r.memo)
                    runEditPlaceSw.isChecked = (r.place == OUTDOOR_RIDE)
                    cal1.time = r.date
                    cal2.time = r.term

                    // find menu name by idを実装すべし (run.menuIDから探しましょう）
                    menuID = r.menuID
                    runEditMenuText.text = findMenuNameByID(applicationContext, menuID)

                    // 編集画面の時のみ、削除ボタンを作る
                    runEditDeleteBtn.setOnClickListener {
                        AlertDialog.Builder(this).apply {
                            setTitle(R.string.del_confirm_dialog_title)
                            setMessage(R.string.del_confirm_dialog_message)
                            setCancelable(true)
                            setNegativeButton(R.string.del_confirm_dialog_cancel, null)
                            setPositiveButton("OK",
                                object : DialogInterface.OnClickListener {
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        realm.executeTransaction { realm.where<RunData>().equalTo("id", r.id)?.findFirst()?.deleteFromRealm() }
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
        // 日付・時刻選択のダイアログボタン用
        val year1   = cal1.get(Calendar.YEAR)
        val month1  = cal1.get(Calendar.MONTH)
        val day1    = cal1.get(Calendar.DAY_OF_MONTH)
        val hour1   = cal2.get(Calendar.HOUR_OF_DAY)
        val min1    = cal2.get(Calendar.MINUTE)

        // 日付・時刻をTextViewに事前にセット
        runEditDateText.text = getString(R.string.date_format).format(year1,month1+1,day1)
        runEditDurationText.text = getString(R.string.time_format).format(hour1,min1)

        // 日付ボタンのリスナ登録と、アンダーライン設定（ほかにやり方ないのかね・・・？）
        runEditDateText.paintFlags = runEditDateText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        runEditDateText.setOnClickListener {
            DatePickerDialog(
                this, DatePickerDialog.OnDateSetListener { view, y, m, d ->
                    runEditDateText.text = getString(R.string.date_format).format(y, m+1, d)
                }, year1, month1, day1
            ).show()
        }
        runEditDurationText.paintFlags = runEditDurationText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        runEditDurationText.setOnClickListener {
            TimePickerDialog(
                this, TimePickerDialog.OnTimeSetListener { view, h, m ->
                    runEditDurationText.text = getString(R.string.time_format).format(h, m)
                }, hour1, min1, true
            ).show()
        }

        // ランメニュー選択ボタン
        runEditMenuText.setOnClickListener {
            val intent = Intent(this, MenuListActivity::class.java)
            intent.putExtra("from", "Run")
            startActivityForResult(intent, REQUEST_CODE_MENU_SELECT)
        }

        // SAVEボタンのリスナ。デカいので外だし
        runEditSaveBtn.setOnClickListener(OKButtonListener())

        // キャンセルボタン
        runEditCancelBtn.setOnClickListener { finish() }

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
            val runDate1 = (runEditDateText.text as String + " 00:00:00").toDate()
            val runDate2 = ("2000/01/01 " + runEditDurationText.text as String).toDate()

            // ゼロチェックしないといけないんだってさ！！
            var s = ""
            s = runEditTssEdit.text.toString()
            val tss = if(s.isNotBlank()) s.toInt() else 0

            s = runEditKmEdit.text.toString()
            val km = if(s.isNotBlank()) s.toInt() else 0

            s = runEditKcalEdit.text.toString()
            val kcal = if(s.isNotBlank()) s.toInt() else 0

            val memo = runEditMemoEdit.text.toString()
            val place = if( runEditPlaceSw.isChecked ) OUTDOOR_RIDE else INDOOR_RIDE

            when( editMode ) {
                RUN_EDIT_MODE_NEW -> {
                    realm.executeTransaction {
                        // whereで最後尾を探し、そこに追記
                        val maxID = realm.where<RunData>().max("id")
                        val nextID = (maxID?.toLong() ?: 0L) + 1L

                        // ここから書き込み
                        val r= realm.createObject<RunData>(nextID)
                        r.date = runDate1
                        r.term = runDate2
                        r.tss   = tss
                        r.km    = km
                        r.kcal  = kcal
                        r.memo  = memo
                        r.place = place
                        r.menuID = menuID
                    }
                }

                RUN_EDIT_MODE_EDIT -> {
                    realm.executeTransaction {
                        val r = realm.where<RunData>().equalTo("id", runID).findFirst()
                        r?.date = runDate1
                        r?.term = runDate2
                        r?.tss = tss
                        r?.km = km
                        r?.kcal = kcal
                        r?.memo = memo
                        r?.place = place
                        r?.menuID = menuID
                    }
                }
            } // editMode

            // これで編集作業完了！
            finish()

        } // onClick
    } // OKListener

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_MENU_SELECT) {
            when (resultCode) {
                RESULT_OK -> {
                    val id = data?.getLongExtra("id", 0L)
                    val name = data?.getStringExtra("name")

                    runEditMenuText.text = name
                    menuID = id as Long
                }
            }
        }
    }

    // 入力箇所（EditText）以外をタップしたときに、フォーカスをオフにする
    // おおもとのLayoutにfocusableInTouchModeをtrueにしないといけない
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        // キーボードを隠す
        inputMethodManager.hideSoftInputFromWindow(runEditLayout.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
        // 背景にフォーカスを移す
        runEditLayout.requestFocus()
        return super.dispatchTouchEvent(event)
    }
}
