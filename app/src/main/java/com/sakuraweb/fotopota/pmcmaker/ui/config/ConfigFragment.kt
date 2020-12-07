package com.sakuraweb.fotopota.pmcmaker.ui.config

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sakuraweb.fotopota.pmcmaker.*
import com.sakuraweb.fotopota.pmcmaker.ui.menu.MENU_DATA_VERSION
import com.sakuraweb.fotopota.pmcmaker.ui.menu.MenuData
import com.sakuraweb.fotopota.pmcmaker.ui.menu.MenuDataMigration
import com.sakuraweb.fotopota.pmcmaker.ui.menu.MenuDataModule
import com.sakuraweb.fotopota.pmcmaker.ui.run.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.io.File


class ConfigFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {

        addPreferencesFromResource(R.xml.root_preferences)

        //　各入力項目を数字限定にする。面倒よねぇ・・・。
        findPreference<EditTextPreference>("pmc_term")?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        findPreference<EditTextPreference>("atl_term")?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        findPreference<EditTextPreference>("ctl_term")?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        findPreference<EditTextPreference>("pmc_y_max")?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        findPreference<EditTextPreference>("pmc_y_min")?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        // バックアップボタン
        findPreference<Preference>("backup")?.setOnPreferenceClickListener {
            var ext = context?.getExternalFilesDir(null).toString()

            // トレーニングリスト
            var realm = Realm.getInstance(runRealmConfig)
            var src = File(realm.path)
            var dst = File(ext + "/" + run_list_backup)
            src.copyTo(dst, overwrite = true)
            realm.close()

            // トレーニングメニューリスト
            realm = Realm.getInstance(menuRealmConfig)
            src = File(realm.path)
            dst = File(ext + "/" + menu_list_backup)
            src.copyTo(dst, overwrite = true)
            realm.close()

            AlertDialog.Builder(context).apply {
                setTitle(R.string.dialog_backup_done_title)
                setMessage( String.format(getString(R.string.dialog_backup_done_message),ext, run_list_backup, menu_list_backup ) )
                setCancelable(true)
                setPositiveButton("OK", object : DialogInterface.OnClickListener { override fun onClick(dialog: DialogInterface?, which: Int) {  } })
                show()
            }

            true
        }


        // レストアボタン
        findPreference<Preference>("restore")?.setOnPreferenceClickListener {
            // トレーニングリスト
            // 既にバックアップ済みのデータが外部メモリにあるかどうか
            val ext = context?.getExternalFilesDir(null).toString()
            var src = File("$ext/$run_list_backup")

            if (src.exists()) {
                AlertDialog.Builder(context).apply {
                    setTitle(R.string.overwrite_confirm_dialog_title)
                    setMessage(R.string.overwrite_confirm_dialog_message_run)
                    setCancelable(true)
                    setNegativeButton(R.string.overwrite_confirm_dialog_cancel, null)
                    setPositiveButton("OK",
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {

                                val srcRealmConfig = RealmConfiguration.Builder()
                                    .name(run_list_backup)
                                    .directory(File(ext))
                                    .modules(RunDataModule())
                                    .schemaVersion(RUN_DATA_VERSION)
                                    .migration(RunDataMigration())
                                    .build()
                                val srcRealm = Realm.getInstance(srcRealmConfig)
                                val dstRealm = Realm.getInstance(runRealmConfig)

                                dstRealm.beginTransaction()
                                dstRealm.deleteAll()
                                val temp = srcRealm.where<RunData>().findAll()

                                for( t in temp ) {
                                    var d = dstRealm.createObject<RunData>(t.id)
                                    d.date=t.date; d.term=t.term; d.tss=t.tss; d.kcal=t.kcal; d.km=t.km; d.place=t.place; d.memo=t.memo; d.menuID=t.menuID
                                }
                                dstRealm.commitTransaction()

                                dstRealm.close()
                                srcRealm.close()
                                blackToast(context, getString(R.string.overwrite_confirm_dialog_done_run))
                            }
                        })
                    show()
                }
            } // トレーニングデータのレストア終了
            else {
                AlertDialog.Builder(context).apply {
                    setTitle(R.string.dialog_backup_nothing_title)
                    setMessage( String.format(getString(R.string.dialog_backup_run_nothing_message),ext, run_list_backup ) )
                    setCancelable(true)
                    setPositiveButton("OK", object : DialogInterface.OnClickListener { override fun onClick(dialog: DialogInterface?, which: Int) {  } })
                    show()
                }
            }


            // メニューリスト
            // 既にバックアップ済みのデータが外部メモリにあるかどうか
            src = File("$ext/$menu_list_backup")
            if (src.exists()) {
                AlertDialog.Builder(context).apply {
                    setTitle(R.string.overwrite_confirm_dialog_title)
                    setMessage(R.string.overwrite_confirm_dialog_message_menu)
                    setCancelable(true)
                    setNegativeButton(R.string.overwrite_confirm_dialog_cancel, null)
                    setPositiveButton("OK",
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {

                                val srcRealmConfig = RealmConfiguration.Builder()
                                    .name(menu_list_backup)
                                    .directory(File(ext))
                                    .modules(MenuDataModule())
                                    .schemaVersion(MENU_DATA_VERSION)
                                    .migration(MenuDataMigration())
                                    .build()
                                val srcRealm = Realm.getInstance(srcRealmConfig)
                                val dstRealm = Realm.getInstance(menuRealmConfig)

                                dstRealm.beginTransaction()
                                dstRealm.deleteAll()
                                val temp = srcRealm.where<MenuData>().findAll()

                                for( t in temp ) {
                                    var d = dstRealm.createObject<MenuData>(t.id)
                                    d.name  = t.name
                                    d.desc  = t.desc
                                }
                                dstRealm.commitTransaction()
                                dstRealm.close()
                                srcRealm.close()
                                blackToast(context, getString(R.string.overwrite_confirm_dialog_done_menu))
                            }
                        })
                    show()
                }
            } // メニューリストのレストア終了
            else {
                AlertDialog.Builder(context).apply {
                    setTitle(R.string.dialog_backup_nothing_title)
                    setMessage( String.format(getString(R.string.dialog_backup_menu_nothing_message),ext, menu_list_backup ) )
                    setCancelable(true)
                    setPositiveButton("OK", object : DialogInterface.OnClickListener { override fun onClick(dialog: DialogInterface?, which: Int) {  } })
                    show()
                }
            }

            true
        }

/*
        findPreference<Preference>("restore")?.setOnPreferenceClickListener {
            // ランリスト
            var realm = Realm.getInstance(runRealmConfig)
            var dst = File(realm.path)

            var src = File(context?.getExternalFilesDir(null).toString() + "/" + run_list_backup)
            if( src.exists() ) {
                AlertDialog.Builder(context).apply {
                    setTitle(R.string.overwrite_confirm_dialog_title)
                    setMessage(R.string.overwrite_confirm_dialog_message_run)
                    setCancelable(true)
                    setNegativeButton(R.string.overwrite_confirm_dialog_cancel, null)
                    setPositiveButton("OK",
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {

                                realm.beginTransaction()
                                realm.delete
                                src.copyTo(dst, overwrite = true)
                                realm.commitTransaction()

                                blackToast(context, getString(R.string.overwrite_confirm_dialog_done_run))
                            }
                        })
                    show()
                }


            }
            realm.close()


            true
        }
*/

//        root.setMenuBtn.setOnClickListener {
//            val intent = Intent(activity, MenuListActivity::class.java)
//            startActivity(intent)
//        }
    }

}
