package com.sakuraweb.fotopota.pmcmaker

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.sakuraweb.fotopota.pmcmaker.ui.menu.*
import com.sakuraweb.fotopota.pmcmaker.ui.run.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// グローバル変数たち
lateinit var runRealmConfig: RealmConfiguration     // 走行（トレーニング）データベース
lateinit var menuRealmConfig: RealmConfiguration    // トレーニングメニューデータベース
const val run_list_backup   = "training_list_backup.realm"
const val menu_list_backup  = "menu_list_backup.realm"

lateinit var placeList : Array<String>              // 実施場所（INDOOR_RIDE:インドア、OUTDOOR_RIDE:外）

// 一番最初に実行されるApplicationクラス
// いつもの、AppCompatActivity（MainActivity）は、manifest.xmlで最初の画面（Activity）として実行される
// Application（CustomApplication）も、manifest.xmlで最初のクラスとして実行される
// で、その実行順位が、Application ＞ AppCompatActivityとなっているので、こっちの方が先
// 今回は、データベース作成のために最初にここで起動させる

object ApplicationController : Application() {
   private var sInstance: ApplicationController = this

    override fun onCreate() {
        super.onCreate()
        sInstance = this;
    }

    fun getInstance() : ApplicationController {
        return sInstance
    }
}

class StartApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // あちこちで使う定数たち
        placeList = resources.getStringArray(R.array.place_list)

        // Realm全体の初期化処理
        Realm.init(applicationContext)

        // サンプルデータを作ってみる
        createRunData()
        createMenuData()
    }
    private fun createMenuData() {
        // configを設定
        menuRealmConfig = RealmConfiguration.Builder()
            .name("menu.realm")
            .modules(MenuDataModule())
            .schemaVersion(MENU_DATA_VERSION)
            .migration(MenuDataMigration())
            .build()

        // インスタンス化
        val realm = Realm.getInstance(menuRealmConfig)
        val menus: RealmResults<MenuData> = realm.where<MenuData>().findAll()

        if( menus.size == 0 ) {
            val menuList = listOf<MenuDataInit> (
                MenuDataInit("2x15 FTP Intervals",  getString(R.string.menu_2x15ftp)),
                MenuDataInit("3x15 FPT Intervals",  getString(R.string.menu_3x15ftp)),
                MenuDataInit("SST30min",            getString(R.string.menu_SST30min)),
                MenuDataInit("SST45min",            getString(R.string.menu_SST45min)),
                MenuDataInit("SST60min",            getString(R.string.menu_SST60min)),
                MenuDataInit("TABATA",              getString(R.string.menu_tabata)),
                MenuDataInit("FPT TEST(short)",     getString(R.string.menu_fpt_test_short)),
                MenuDataInit("FPT TEST(full)",      getString(R.string.menu_fpt_test_long))
            )
            // DB書き込み
            realm.beginTransaction()
            var id = 1
            for (i in menuList.reversed()) {
                val m = realm.createObject<MenuData>(id++)
                m.name = i.name
                m.desc = i.desc
            }
            realm.commitTransaction()
        }
        realm.close()
    }

    private fun createRunData() {
        // configを設定
        runRealmConfig = RealmConfiguration.Builder()
            .name("run.realm")
            .modules(RunDataModule())
            .schemaVersion(RUN_DATA_VERSION)
            .migration(RunDataMigration())
            .build()

        // インスタンス化
        val realm = Realm.getInstance(runRealmConfig)
        val runs: RealmResults<RunData> = realm.where<RunData>().findAll()

        // データ数ゼロならサンプルを作る
        if ( runs.size == -1 ) {
            val runList = listOf<RunDataInit>(
                RunDataInit("2020/10/29", "2020/11/1 1:00", 80, 300, 100, INDOOR_RIDE, "Very Hard !!", 1),
                RunDataInit("2020/10/30", "2020/11/1 1:00", 80, 300, 100, INDOOR_RIDE, "Very Hard !!", 0),
                RunDataInit("2020/11/1", "2020/11/1 1:00", 80, 300, 100, INDOOR_RIDE, "Very Hard !!",1),
                RunDataInit("2020/11/2", "2020/11/1 1:00", 10, 300, 100, INDOOR_RIDE, "Very Hard !!",2),
                RunDataInit("2020/11/3", "2020/11/1 1:00", 80, 300, 100, INDOOR_RIDE, "Very Hard !!",1),
                RunDataInit("2020/11/4", "2020/11/1 1:00", 20, 300, 100, INDOOR_RIDE, "Very Hard !!",0),
                RunDataInit("2020/11/5", "2020/11/1 3:00", 30, 300, 100, INDOOR_RIDE, "かなりの長時間",0),
                RunDataInit("2020/11/6", "2020/11/1 2:00", 10, 300, 100, INDOOR_RIDE, "色々大変",0),
                RunDataInit("2020/11/7", "2020/11/1 1:00", 50, 300, 100, INDOOR_RIDE, "",1),
                RunDataInit("2020/11/8", "2020/11/1 1:00", 10, 300, 100, INDOOR_RIDE, "",1),
                RunDataInit("2020/11/9", "2020/11/1 1:00", 20, 300, 100, OUTDOOR_RIDE, "屋外",2),
                RunDataInit("2020/11/10", "2020/11/1 1:00", 80, 300, 100, INDOOR_RIDE, "1時間は長い",2),
                RunDataInit("2020/11/11", "2020/11/1 1:00", 80, 300, 100, INDOOR_RIDE, "疲労こそ命",0),
                RunDataInit("2020/11/12", "2020/11/1 1:00", 40, 300, 100, INDOOR_RIDE, "very hard",1),
                RunDataInit("2020/11/15", "2020/11/1 4:00", 80, 300, 100, INDOOR_RIDE, "want to cry",2),
                RunDataInit("2020/11/16", "2020/11/5 1:30", 10, 30, 10, OUTDOOR_RIDE, "want to quit",1),
                RunDataInit("2020/11/17", "2020/11/5 1:30", 20, 30, 10, OUTDOOR_RIDE, "little hard",2),
                RunDataInit("2020/11/18", "2020/11/5 1:30", 30, 30, 10, OUTDOOR_RIDE, "recovery",2),
                RunDataInit("2020/11/19", "2020/11/5 1:30", 10, 30, 10, OUTDOOR_RIDE, "Extremely hard...",1)
            )
            // DB書き込み
            realm.beginTransaction()
            var id = 1
            for (i in runList.reversed()) {
                val t = realm.createObject<RunData>(id++)
                t.date = i.date.toDate("yyyy/MM/dd")
                t.term = i.term.toDate("yyyy/MM/dd HH:mm")
                t.tss = i.tss
                t.km = i.km
                t.kcal = i.kcal
                t.place = i.place
                t.memo = i.memo
                t.menuID = i.menu
            }
            realm.commitTransaction()
        }
        realm.close()
    }

    public fun backupData() {
        // ランリスト
        var realm = Realm.getInstance(runRealmConfig)
        var src = File(realm.path)
        var dst = File(getExternalFilesDir(null).toString() + "/" + run_list_backup)
        src.copyTo(dst, overwrite = true)
        realm.close()

        // トレーニングメニューリスト
        realm = Realm.getInstance(menuRealmConfig)
        src = File(realm.path)
        dst = File(applicationContext.getExternalFilesDir(null).toString() + "/" + menu_list_backup)
        src.copyTo(dst, overwrite = true)
        realm.close()

        blackToast(applicationContext, "バックアップ完了！")
    }
}


fun String.toDate(pattern: String = "yyyy/MM/dd HH:mm"): Date {
    val df = SimpleDateFormat(pattern)
    return df.parse(this)
}

// 黒いToast画面を出すだけ
public fun blackToast(c: Context, s: String) {
    val toast = Toast.makeText(c, s, Toast.LENGTH_SHORT)
    val view: View? = toast.view

//    view.background.setColorFilter(Color.rgb(0,0,0), PorterDuff.Mode.SRC_IN)
    view?.let {
        it.background.colorFilter =
            PorterDuffColorFilter(Color.rgb(0, 0, 0), PorterDuff.Mode.SRC_IN)
        it.findViewById<TextView>(android.R.id.message)?.setTextColor(Color.rgb(255, 255, 255))
    }
    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0)
    toast.show()
}

