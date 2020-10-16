package com.sakuraweb.fotopota.pmcmaker.ui.pmc

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.blackToast
import com.sakuraweb.fotopota.pmcmaker.runRealmConfig
import com.sakuraweb.fotopota.pmcmaker.ui.run.RunData
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where

import kotlinx.android.synthetic.main.pmc_fragment.*
import java.util.*
import kotlin.math.exp

// TODO: 診断のON/OFFボタンを付けたい。多すぎる
// TODO: なんども配列を作るせいか、非常～～～に遅い！ グラフかもしれないけど

// RunDataのRealm
lateinit var runs: RealmResults<RunData>

var atlTerm: Int = 0
var ctlTerm: Int = 0
var pmcTerm: Int = 0

// PMCグラフを描くための各データ
lateinit var atlList: Array<Float>
lateinit var ctlList: Array<Float>
lateinit var tsbList: Array<Float>
var pmcYMax : Float = 60.0F
var pmcYmin : Float = -60.0F

// 診断に使うときは、逆順の方が楽なので向きを逆に・・・ ちゃんとやったほうがいいか？
lateinit var revATLs: List<Float>
lateinit var revCTLs: List<Float>
lateinit var revTSBs: List<Float>

class PmcFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // onCreateではとりあえずコンテナを作るだけで終了～
        return inflater.inflate(R.layout.pmc_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()

        // onStartから、実際にPMCを描いたり、DIAGをしたりする

        // まずは全期間の TSS/ATL/CTL/TSBを作る
        val realm = Realm.getInstance(runRealmConfig)
        runs = realm.where<RunData>().findAll().sort("date", Sort.ASCENDING)
        // 一応ゼロチェック
        if( runs.size==0 ) return

        // ーーーーーーーーーー　各期間を設定情報から読み込んでおく　ーーーーーーーーーー
        PreferenceManager.getDefaultSharedPreferences(activity).apply {
            getString("atl_term", "7")?.let { atlTerm = it.toInt() }
            getString("ctl_term", "42")?.let { ctlTerm = it.toInt() }
            getString("pmc_term", "31")?.let { pmcTerm = it.toInt() }
            getString("pmc_y_max", "60")?.let { pmcYMax = it.toFloat() }
            pmcYmin = -1 * pmcYMax
        }

        // PMCを描いて、Realmを閉める
        drawPMC()
        realm.close()

        // ここから、各DIAGをやり始める
        revATLs = atlList.reversed(); revTSBs = tsbList.reversed(); revCTLs = ctlList.reversed()

        PreferenceManager.getDefaultSharedPreferences(activity).apply {
            if(getBoolean("diag1_sw", true)) addCard( drawDIAG1(),"diag1_sw")
            if(getBoolean("diag2_sw", true)) addCard( drawDIAG2(),"diag2_sw")
            if(getBoolean("diag3_sw", true)) addCard( drawDIAG3(),"diag3_sw" )
            if(getBoolean("diag4_sw", true)) addCard( drawDIAG4(),"diag4_sw" )
            if(getBoolean("diag5_sw", true)) addCard( drawDIAG5(),"diag5_sw" )
            if(getBoolean("diag6_sw", true)) addCard( drawDIAG6(),"diag6_sw" )
            if(getBoolean("diag7_sw", true)) addCard( drawDIAG7(),"diag7_sw" )
            if(getBoolean("diag8_sw", true)) addCard( drawDIAG8(),"diag8_sw" )
        }
    }

    private fun addCard( data:DiagDrawData, sw:String ) {
        // DIAGを作るサブルーチン（レイアウトパラメータ、診断パラメータから作る）
        val newLayout = activity?.layoutInflater?.inflate(R.layout.diag_card, null)
        newLayout?.apply {
            findViewById<TextView>(R.id.diagCard1Title).setBackgroundColor(resources.getColor(data.colorID))
            findViewById<TextView>(R.id.diagCard1Title).text        = getString(data.titleID)
            findViewById<TextView>(R.id.diagCard1SubTitle).text     = getString(data.subTitleID)
            findViewById<TextView>(R.id.diagCard1Unit).text         = getString(data.unitID)
            findViewById<TextView>(R.id.diagCard1Description).text  = getString(data.descriptionID)

            findViewById<ImageView>(R.id.diagCardIcon).setImageResource(data.iconID)
            findViewById<TextView>(R.id.diagCardScore).text         = data.score
            findViewById<TextView>(R.id.diagCardMsg).text           = getString(data.messageID)

            // カードごとについているHideボタンの処理
            findViewById<Button>(R.id.diagHideButton).setOnClickListener {
                AlertDialog.Builder(activity).apply {
                    setTitle(R.string.hide_confirm_dialog_title); setMessage(R.string.hide_confirm_dialog_message); setCancelable(true)
                    setNegativeButton(R.string.hide_confirm_dialog_no, null)
                    setPositiveButton("OK",
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                // やっとこ本題。設定項目をfalseにし、表示済みのカードはGONEにして消しちゃう
                                PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean(sw, false).apply()
                                newLayout.visibility = View.GONE
                            }
                        })
                    show()
                }
            }
        }
        diagLayout.addView( newLayout )
    }

    private fun drawPMC() {
        // PMCグラフを描きましょう
        chartArea1.xAxis.position = XAxis.XAxisPosition.BOTTOM

        // X軸を作る
        chartArea1.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            labelRotationAngle = 270F
            setDrawGridLines(true)
            enableGridDashedLine(10f,10f,0f)
            setDrawAxisLine(true)
        }

        // Y軸（左）
        chartArea1.axisLeft.apply {
            setDrawLabels(true)
            setAxisMaxValue(pmcYMax)
            setAxisMinValue(pmcYmin)
            setLabelCount(7, true)
            enableGridDashedLine(10f,10f,0f)
            setDrawZeroLine(true)
            spaceBottom = 0F
            granularity = 1F
        }

        // Y軸（右）
        chartArea1.axisRight.apply {
            setDrawLabels(true)
            setAxisMaxValue(600F)
            setAxisMinValue(0F)
            setLabelCount(7, true)
            enableGridDashedLine(10f,10f,0f)
            setDrawZeroLine(true)
            spaceBottom = 0F
            granularity = 1F
        }

        chartArea1.apply {
            isClickable = false
            setDescription("")
            setPinchZoom(false)

            legend.setPosition( Legend.LegendPosition.ABOVE_CHART_CENTER )

            isDoubleTapToZoomEnabled = false
            //アニメーション
            animateY(1000, Easing.EasingOption.Linear);
            // データ作成は別関数で
            data = createBarGraphData()
            if( data==null ) return
            invalidate()
        }

        pmcRecentText.text = getString(R.string.pmc_recent_values)
            .format(atlList[atlList.size-1].toInt(), ctlList[ctlList.size-1].toInt(), tsbList[tsbList.size-1].toInt())

    }

    private fun createBarGraphData() : CombinedData? {
        val ma: Activity = activity as Activity

        // PMC期間設定　←　もしかして時分秒のクリアが必要かも。やってないけど
        var begin = Calendar.getInstance()
        begin.add( Calendar.DAY_OF_MONTH, -1*(pmcTerm))
        var end = Calendar.getInstance()

        // １．開始期間を設定（いずれCONFIG化する
        val org = Calendar.getInstance()
        org.time = runs[0]?.date
        val term = ( (end.timeInMillis - org.timeInMillis) / (1000*24*60*60) ).toInt() + 1

        // ２．全期間のTSSリスト作成
        var tssList = IntArray( term )
        var rd = Calendar.getInstance()

        for( r in runs) {
            rd.time = r.date
            rd.set( Calendar.HOUR_OF_DAY, 0)
            rd.set( Calendar.MINUTE, 0)
            rd.set( Calendar.SECOND, 0)
            var diff = (rd.timeInMillis - org.timeInMillis) / (1000*24*60*60)
            tssList[ diff.toInt() ] += r.tss
        }

        // ３．ATL/CTL/TSBを作る
        atlList = Array<Float>( term ) { 0F }
        ctlList = Array<Float>( term ) { 0F }
        tsbList = Array<Float>( term ) { 0F }

        // EXPを定数化して少しは速くしてみる
        val atlExp = exp(-1.0 / atlTerm)
        val ctlExp = exp(-1.0 / ctlTerm)

        atlList[0] = ( (1- atlExp) * tssList[0] ).toFloat()
        ctlList[0] = ( (1- ctlExp) * tssList[0] ).toFloat()
        tsbList[0] = 0F
        for( i in 1..tssList.size-1) {
            atlList[i] = ( (1-atlExp) * tssList[i] + atlList[i-1] * atlExp ).toFloat()
            ctlList[i] = ( (1-ctlExp) * tssList[i] + ctlList[i-1] * ctlExp ).toFloat()
            tsbList[i] = ctlList[i-1] - atlList[i-1]
        }

        // 全期間のTSS/ATL/CTL/TSBが完成したので、指定期間のPMCを作り始める
        var xLabels = Array<String>( pmcTerm ){ "" }
        var bc = Calendar.getInstance()
        bc = begin
        bc.add( Calendar.DAY_OF_MONTH, 1 )

        // １．各要素のデータ配列
        val tssValues = arrayOfNulls<BarEntry>( pmcTerm )
        val atlValues = arrayOfNulls<Entry>( pmcTerm )
        val ctlValues = arrayOfNulls<Entry>( pmcTerm )
        val tsbValues = arrayOfNulls<Entry>( pmcTerm )

        for( i in 0..pmcTerm-1 ) {
            xLabels[i] = "%2d/%2d".format(bc.get(Calendar.MONTH)+1, bc.get(Calendar.DAY_OF_MONTH))
            bc.add( Calendar.DAY_OF_MONTH, 1)
            val index = i + term - pmcTerm
            tssValues[i] = BarEntry( if(index>=0)  tssList[index].toFloat() else 0F, i )
            atlValues[i] = Entry( if(index>=0) atlList[index] else 0F, i )
            ctlValues[i] = Entry( if(index>=0) ctlList[index] else 0F, i )
            tsbValues[i] = Entry( if(index>=0) tsbList[index] else 0F, i )
        }

        // CTLカーブ
        val ctlLineDataSet = LineDataSet(ctlValues.toMutableList(), getString(R.string.pmc_ctl_name)).apply {
            setDrawValues(false)
            lineWidth = 4F
            color = ma.getColor(R.color.ctlBarColor)
            axisDependency = YAxis.AxisDependency.LEFT

            setDrawFilled(true)
            isHighlightEnabled = true
            setCircleColor( ma.getColor(R.color.ctlBarColor) )
            setDrawCircles(true)
            setDrawCircleHole(true)
            circleRadius = 3f
//            setDrawValues( true)

/*        setFillFormatter(object : IFillFormatter() {
            fun getFillLinePosition(
                dataSet: ILineDataSet?,
                dataProvider: LineDataProvider?
            ): Float {
                return chart.getAxisLeft().getAxisMinimum()
            }
        })
        */
        }

        // TSBカーブ
        val tsbLineDataSet = LineDataSet(tsbValues.toMutableList(), getString(R.string.pmc_tsb_name)).apply {
            setDrawValues(false)
            setDrawCircles(false)
            lineWidth = 2F
            color = ma.getColor(R.color.tsbBarColor)
            axisDependency = YAxis.AxisDependency.LEFT
        }

        // ATLカーブ
        val atlLineDataSet = LineDataSet(atlValues.toMutableList(), getString(R.string.pmc_atl_name)).apply {
            setDrawValues(false)
            setDrawCircles(false)
            lineWidth = 2F
            color = ma.getColor(R.color.atlBarColor)
            axisDependency = YAxis.AxisDependency.LEFT
        }

        // TSSバー
        val barDataSet1 = BarDataSet(tssValues.toMutableList(), getString(R.string.pmc_tss_name)).apply {
            setDrawValues(false)
            color = ma.getColor(R.color.tssBarColor)
            axisDependency = YAxis.AxisDependency.RIGHT
        }

        val barData = BarData( xLabels, barDataSet1 )

        val lineData = LineData( xLabels, atlLineDataSet )
        lineData.addDataSet( ctlLineDataSet )
        lineData.addDataSet( tsbLineDataSet )

        val data = CombinedData( xLabels )
        data.setData(barData)
        data.setData(lineData)

        return data
    }
}

