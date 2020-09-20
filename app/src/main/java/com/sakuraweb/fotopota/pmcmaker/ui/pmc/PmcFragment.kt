package com.sakuraweb.fotopota.pmcmaker.ui.pmc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.utils.ColorTemplate
import com.sakuraweb.fotopota.pmcmaker.R
import com.sakuraweb.fotopota.pmcmaker.runRealmConfig
import com.sakuraweb.fotopota.pmcmaker.ui.run.*
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.pmc_fragment.*
import java.util.*
import kotlin.math.exp

var atlTerm: Int = 0
var ctlTerm: Int = 0
var pmcTerm: Int = 0

//lateinit var tssList: Array<Int?>
//lateinit var dateList: Array<Date> // 要らないような

class PmcFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.pmc_fragment, container, false)

        return root
    }

    override fun onStart() {
        super.onStart()

        // ーーーーーーーーーー　各期間を設定情報から読み込んでおく　ーーーーーーーーーー
        PreferenceManager.getDefaultSharedPreferences(activity).apply {
            getString("atl_term", "0")?.let { atlTerm = it.toInt() }
            getString("ctl_term", "0")?.let { ctlTerm = it.toInt() }
            getString("pmc_term", "0")?.let { pmcTerm = it.toInt() }
        }

        text1.text = "ATL:%d".format(atlTerm)
        text2.text = "CTL:%d".format(ctlTerm)
        text3.text = "PMC:%d".format(pmcTerm)


        chartArea1.xAxis.position = XAxis.XAxisPosition.BOTTOM

        // X軸を作る
        chartArea1.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawAxisLine(true)
        }

        // Y軸（左）
        chartArea1.axisLeft.apply {
            setDrawLabels(true)
            spaceBottom = 0F
            granularity = 1F
        }

        // Y軸（右）
        chartArea1.axisRight.apply {
            setDrawLabels(true)
            spaceBottom = 0F
            granularity = 1F
        }

        chartArea1.apply {
            isClickable = false
            setDescription("PMC")
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            //アニメーション
            animateY(1000, Easing.EasingOption.Linear);
            // データ作成は別関数で
            data = createBarGraphData()
            invalidate()
        }

    }

    private fun createBarGraphData() : CombinedData {

        var begin = Calendar.getInstance()
        begin.add( Calendar.DAY_OF_MONTH, -1*pmcTerm)
        var end = Calendar.getInstance()

        // PMC期間中のTSSリストを作る
        val realm = Realm.getInstance(runRealmConfig)
        val runs = realm.where<RunData>().between("date", begin.time, end.time).findAll().sort("date", Sort.DESCENDING)
        var tssList = Array<Int>( pmcTerm ) {0}
        for( r in runs) tssList[ ((end.time.time - r.date.time) / (1000*60*60*24)).toInt() ] = r.tss

        // ATL/CTL/TSBを作る
        var atlList = FloatArray( pmcTerm )
        var ctlList = FloatArray( pmcTerm )
        var tsbList = FloatArray( pmcTerm )

        for( i in 1..tssList.size-1) {
            atlList[i] = ( exp(-1.0 / atlTerm) * (atlList[i-1]-tssList[i]) + tssList[i] ).toFloat()
            ctlList[i] = ( exp(-1.0 / ctlTerm) * (ctlList[i-1]-tssList[i]) + tssList[i] ).toFloat()
            tsbList[i] = ctlList[i-1] - atlList[i-1]
        }

        // とりあえず適当に
        val xLabels = Array<String>( pmcTerm ) {it.toString()}
        val tssValues = arrayOfNulls<BarEntry>( pmcTerm )
        val atlValues = arrayOfNulls<Entry>( pmcTerm )
        val ctlValues = arrayOfNulls<Entry>( pmcTerm )
        val tsbValues = arrayOfNulls<Entry>( pmcTerm )

        for( i in 0..tssList.size-1 ) {
            xLabels[i] = i.toString()
            tssValues[i] = BarEntry( tssList[i].toFloat(), i )
            atlValues[i] = Entry( atlList[i], i )
            ctlValues[i] = Entry( ctlList[i], i )
            tsbValues[i] = Entry( tsbList[i], i )
        }

        val ctlLineDataSet = LineDataSet(ctlValues.toMutableList(), "CTLdesu").apply {
            setDrawValues(false)
            setDrawCircles(false)
            lineWidth = 1F
            color = ColorTemplate.COLORFUL_COLORS[2]
            axisDependency = YAxis.AxisDependency.RIGHT
        }

        val tsbLineDataSet = LineDataSet(tsbValues.toMutableList(), "TSBdesu").apply {
            setDrawValues(false)
            setDrawCircles(false)
            lineWidth = 3F
            color = ColorTemplate.COLORFUL_COLORS[3]
            axisDependency = YAxis.AxisDependency.RIGHT
        }

        val atlLineDataSet = LineDataSet(atlValues.toMutableList(), "ATLdesu").apply {
            setDrawValues(false)
            setDrawCircles(false)
            lineWidth = 2F
            color = ColorTemplate.COLORFUL_COLORS[2]
            axisDependency = YAxis.AxisDependency.RIGHT
        }

        val barDataSet1 = BarDataSet(tssValues.toMutableList(), "TSSdesu").apply {
            setDrawValues(false)
            color = ColorTemplate.COLORFUL_COLORS[0]
            axisDependency = YAxis.AxisDependency.LEFT
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

