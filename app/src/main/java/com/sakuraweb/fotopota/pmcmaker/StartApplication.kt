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
import java.text.SimpleDateFormat
import java.util.*

// グローバル変数たち
lateinit var runRealmConfig: RealmConfiguration     // 走行（トレーニング）データベース
lateinit var menuRealmConfig: RealmConfiguration    // トレーニングメニューデータベース

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
                MenuDataInit(
                    "SST30min",
                    "スイート・スポット・トレーニング（FTPの88～94%）で30分"
                ),
                MenuDataInit(
                    "SST60min",
                    "スイート・スポット・トレーニング（FTPの88～94%）で60分"
                ),
                MenuDataInit(
                    "FTP30min × 2",
                    "FTP30分を2回やる。かなりハード。"
                ),
                MenuDataInit(
                    "こんな風にあらかじめ入れておくと便利です",
                    "　"
                )
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
        if (runs.size == -1) {
            val runList = listOf<RunDataInit>(
                RunDataInit("2020/9/1", "2020/9/1 1:00", 80, 300, 100, INDOOR_RIDE, "Very Hard !!", 1),
                RunDataInit("2020/9/1", "2020/9/1 1:00", 80, 300, 100, INDOOR_RIDE, "Very Hard !!", 0),
                RunDataInit("2020/9/1", "2020/9/1 1:00", 80, 300, 100, INDOOR_RIDE, "Very Hard !!",1),
                RunDataInit("2020/9/2", "2020/9/1 1:00", 10, 300, 100, INDOOR_RIDE, "Very Hard !!",2),
                RunDataInit("2020/9/3", "2020/9/1 1:00", 80, 300, 100, INDOOR_RIDE, "Very Hard !!",1),
                RunDataInit("2020/9/4", "2020/9/1 1:00", 200, 300, 100, INDOOR_RIDE, "Very Hard !!",0),
                RunDataInit("2020/9/5", "2020/9/1 3:00", 300, 300, 100, INDOOR_RIDE, "かなりの長時間",0),
                RunDataInit("2020/9/6", "2020/9/1 2:00", 10, 300, 100, INDOOR_RIDE, "色々大変",0),
                RunDataInit("2020/9/7", "2020/9/1 1:00", 50, 300, 100, INDOOR_RIDE, "",1),
                RunDataInit("2020/9/8", "2020/9/1 1:00", 100, 300, 100, INDOOR_RIDE, "",1),
                RunDataInit("2020/9/9", "2020/9/1 1:00", 200, 300, 100, OUTDOOR_RIDE, "屋外",2),
                RunDataInit("2020/9/10", "2020/9/1 1:00", 80, 300, 100, INDOOR_RIDE, "1時間は長い",2),
                RunDataInit("2020/9/11", "2020/9/1 1:00", 80, 300, 100, INDOOR_RIDE, "疲労こそ命",0),
                RunDataInit("2020/9/12", "2020/9/1 1:00", 400, 300, 100, INDOOR_RIDE, "very hard",1),
                RunDataInit("2020/9/15", "2020/9/1 4:00", 80, 300, 100, INDOOR_RIDE, "want to cry",2),
                RunDataInit("2020/9/16", "2020/9/5 1:30", 10, 30, 10, OUTDOOR_RIDE, "want to quit",1),
                RunDataInit("2020/9/17", "2020/9/5 1:30", 200, 30, 10, OUTDOOR_RIDE, "little hard",2),
                RunDataInit("2020/9/18", "2020/9/5 1:30", 50, 30, 10, OUTDOOR_RIDE, "recovery",2),
                RunDataInit("2020/9/19", "2020/9/5 1:30", 100, 30, 10, OUTDOOR_RIDE, "Extremely hard...",1),
                RunDataInit("2020/9/20", "2020/9/5 1:30", 250, 30, 10, OUTDOOR_RIDE, "Go to long ride",1),
                RunDataInit("2020/9/21", "2020/9/5 1:30", 100, 30, 10, OUTDOOR_RIDE, "Very Hard",2)
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
