package com.sakuraweb.fotopota.pmcmaker.ui.run

import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmModule
import java.util.*

// 日々のトレーニングデータ

// マイグレーション用
// v0 : 初版

const val RUN_DATA_VERSION = 0L

// この記載と、Configuration時のModules指定をしないと、すべての関連ClassがDB化される
// 個別のClassのバージョンアップができないので、こうやって単独化させてあげる
@RealmModule(classes = [RunData::class])
class RunDataModule


const val INDOOR_RIDE = 0
const val OUTDOOR_RIDE = 1


open class RunData : RealmObject() {
    @PrimaryKey
    var id: Long = 0

    lateinit var date: Date    // 実施日
    lateinit var term: Date    // 継続時間

    var tss     : Int  = 0 // もちろんTSS
    var kcal    : Int  = 0 // 消費エネルギー
    var km      : Int  = 0 // 走行距離
    var place   : Int  = -1    // 実施場所（INDOOR_RIDE:インドア、OUTDOOR_RIDE:外）
    var memo    : String = ""   // メモ
    var menuID  : Long = -1   // トレーニングメニューＩＤ
}


// データベース構造（名称だけでも）に変更があった場合のMigration処理
// 初期バージョン（0）から順に最新版までたどって、versionを上げていく。すごいねぇ・・・。
class RunDataMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        val realmSchema = realm.schema
        var oldVersion = oldVersion

//        if( oldVersion==0L ) {
//            realmSchema.get("BrewData")!!
//                .addField("takeoutID", Long::class.java)
//            oldVersion++
//        }
    }
}

class RunDataInit (
    var date    : String,
    var term    : String,
    var tss     : Int,
    var kcal    : Int,
    var km      : Int,
    var place   : Int,
    var memo    : String,
    var menu    : Long
)
{}
