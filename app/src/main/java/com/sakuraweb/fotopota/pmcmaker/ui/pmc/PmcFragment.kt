package com.sakuraweb.fotopota.pmcmaker.ui.pmc

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.os.LocaleList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.runRealmConfig
import com.sakuraweb.fotopota.pmcmaker.ui.run.RunData
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.pmc_fragment.*
import java.util.*
import kotlin.math.exp

// TODO: RecyclerView方式にするのは？ 余計なファイルが増えて帰って面倒か・・・。
// TODO: OKにもNGにも引っかからないDIAGどうする？ 存在を消しちゃおうかね（View.GONE）
// TODO: Cardが増えると画面を出すのが重いかも。 View.GONEじゃなくて、使うときだけ動的にInflateが理想
// TODO: 診断のON/OFF自体を付けてもいいかも。全部で10こもあるのでCONFIGで
// TODO: DIAGのTSB2だけ適正化できている。右上のアイコン＆コメントが整列できている。他のも真似して！


var atlTerm: Int = 0
var ctlTerm: Int = 0
var pmcTerm: Int = 0

lateinit var atlList: Array<Float>
lateinit var ctlList: Array<Float>
lateinit var tsbList: Array<Float>

class PmcFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.pmc_fragment, container, false)

        return root
    }

    override fun onStart() {
        super.onStart()

        // ーーーーーーーーーー　各期間を設定情報から読み込んでおく　ーーーーーーーーーー
        PreferenceManager.getDefaultSharedPreferences(activity).apply {
            getString("atl_term", "7")?.let { atlTerm = it.toInt() }
            getString("ctl_term", "42")?.let { ctlTerm = it.toInt() }
            getString("pmc_term", "31")?.let { pmcTerm = it.toInt() }
        }

        drawPMC()
        drawDIAG()
    }

    private fun drawDIAG() {

    }
    private fun drawPMC() {

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
            setAxisMaxValue(200F)
            setAxisMinValue(-200F)
            setLabelCount(9, true)
            enableGridDashedLine(10f,10f,0f)
            setDrawZeroLine(true)
            spaceBottom = 0F
            granularity = 1F
        }

        // Y軸（右）
        chartArea1.axisRight.apply {
            setDrawLabels(true)
            setAxisMaxValue(800F)
            setAxisMinValue(0F)
            setLabelCount(9, true)
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
            invalidate()
        }

        pmcRecentText.text = getString(R.string.pmc_recent_values)
            .format(atlList[atlList.size-1].toInt(), ctlList[ctlList.size-1].toInt(), tsbList[tsbList.size-1].toInt())

    }

    private fun createBarGraphData() : CombinedData {
        val ma: Activity = activity as Activity

        // PMC期間設定　←　もしかして時分秒のクリアが必要かも。やってないけど
        var begin = Calendar.getInstance()
        begin.add( Calendar.DAY_OF_MONTH, -1*(pmcTerm))
        var end = Calendar.getInstance()

        // まずは全期間の TSS/ATL/CTL/TSBを作る
        val realm = Realm.getInstance(runRealmConfig)
        val runs = realm.where<RunData>().findAll().sort("date", Sort.ASCENDING)

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

        for( i in 1..tssList.size-1) {
            atlList[i] = ( exp(-1.0 / atlTerm) * (atlList[i-1]-tssList[i]) + tssList[i] ).toFloat()
            ctlList[i] = ( exp(-1.0 / ctlTerm) * (ctlList[i-1]-tssList[i]) + tssList[i] ).toFloat()
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
            xLabels[i] = "%2d/%2d".format(bc.get(Calendar.MONTH), bc.get(Calendar.DAY_OF_MONTH))
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

        realm.close()
        return data

    }
}

