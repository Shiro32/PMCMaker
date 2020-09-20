package com.sakuraweb.fotopota.pmcmaker

import android.app.Application
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
                    "SST45min",
                    "スイート・スポット・トレーニング（FTPの88～94%）で45分"
                ),
                MenuDataInit(
                    "SST60min",
                    "スイート・スポット・トレーニング（FTPの88～94%）で60分"
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
        if (runs.size == 0) {
            val runList = listOf<RunDataInit>(
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/2 23:00", "2020/9/1 23:30", 100, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/3 23:00", "2020/9/1 23:30", 20, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/4 23:00", "2020/9/1 23:30", 30, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/5 23:00", "2020/9/1 23:30", 300, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/6 23:00", "2020/9/1 23:30", 30, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/7 23:00", "2020/9/1 23:30", 20, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/8 23:00", "2020/9/1 23:30", 10, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/9 23:00", "2020/9/1 23:30", 100, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/10 23:00", "2020/9/1 23:30", 50, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/11 23:00", "2020/9/1 23:30", 300, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/12 23:00", "2020/9/1 23:30", 270, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/13 23:00", "2020/9/1 23:30", 30, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/14 23:00", "2020/9/1 23:30", 10, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/15 23:00", "2020/9/1 23:30", 80, 300, 100, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/16 10:00", "2020/9/5 12:30", 10, 30, 10, OUTDOOR_RIDE, "最高にキツイ")
            )
            // DB書き込み
            realm.beginTransaction()
            var id = 1
            for (i in runList.reversed()) {
                val t = realm.createObject<RunData>(id++)
                t.date = i.date.toDate("yyyy/MM/dd HH:mm")
                t.term = i.term.toDate("yyyy/MM/dd HH:mm")
                t.tss = i.tss
                t.km = i.km
                t.kcal = i.kcal
                t.place = i.place
                t.memo = i.memo
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