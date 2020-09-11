package com.sakuraweb.fotopota.pmcmaker

import android.app.Application
import com.sakuraweb.fotopota.pmcmaker.ui.run.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.createObject
import java.text.SimpleDateFormat
import java.util.*

// グローバル変数たち
lateinit var trainingRealmConfig: RealmConfiguration

lateinit var placeList : Array<String>

// 一番最初に実行されるApplicationクラス
// いつもの、AppCompatActivity（MainActivity）は、manifest.xmlで最初の画面（Acitivity）として実行される
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

        // トレーニングのサンプルデータを作ってみる
        createTrainingData()
    }

    private fun createTrainingData() {
        // configを設定
        trainingRealmConfig = RealmConfiguration.Builder()
            .name("training.realm")
            .modules(RunDataModule())
            .schemaVersion(RUN_DATA_VERSION)
            .migration(RunDataMigration())
            .build()

        // インスタンス化
        val realm = Realm.getInstance(trainingRealmConfig)
        val trainings: RealmResults<RunData> = realm.where(
            RunData::class.java
        ).findAll().sort("id", Sort.DESCENDING)

        // データ数ゼロならサンプルを作る
        if (trainings.size == 0) {
            val trainingList = listOf<RunDataInit>(
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/1 23:00", "2020/9/1 23:30", 50F, 300F, 100F, INDOOR_RIDE, "最高にキツイ"),
                RunDataInit("2020/9/5 10:00", "2020/9/5 12:30", 10F, 30F, 10F, OUTDOOR_RIDE, "最高にキツイ")
            )
            // DB書き込み
            realm.beginTransaction()
            var id = 1
            for (i in trainingList.reversed()) {
                var t = realm.createObject<RunData>(id++)
                t.date1 = i.date1.toDate("yyyy/MM/dd HH:mm")
                t.date2 = i.date2.toDate("yyyy/MM/dd HH:mm")
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
    val dt = df.parse(this)
    return dt
}