package com.sakuraweb.fotopota.pmcmaker.ui.menu

import io.realm.DynamicRealm
import io.realm.RealmMigration
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmModule

// トレーニングメニューのデータ（Realm）
// ほぼ名称だけのリストだけど将来的な余裕も見て、RecyclerViewで実装する

// マイグレーション用
// v0 : 初版

const val MENU_DATA_VERSION = 0L

// この記載と、Configuration時のModules指定をしないと、すべての関連ClassがDB化される
// 個別のClassのバージョンアップができないので、こうやって単独化させてあげる
@RealmModule(classes = [MenuData::class])
class MenuDataModule

// ↑本当にこの書き方で合っているか？


open class MenuData : RealmObject() {
    @PrimaryKey
    var id: Long = 0

    var name    : String  = "" // メニュー名称
    var desc    : String  = "" // メニューの説明（FTPで30分など）
}


// データベース構造（名称だけでも）に変更があった場合のMigration処理
// まずありえないと思うけど（－－
class MenuDataMigration : RealmMigration {
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

class MenuDataInit (
    var name    : String,
    var desc    : String
)
{}
