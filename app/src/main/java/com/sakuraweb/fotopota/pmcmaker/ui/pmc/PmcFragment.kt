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
            getString("atl_term", "7")?.let { atlTerm = it.toInt() }
            getString("ctl_term", "42")?.let { ctlTerm = it.toInt() }
            getString("pmc_term", "31")?.let { pmcTerm = it.toInt() }
        }

        text1.text = "ATL accumulation term : %d".format(atlTerm)
        text2.text = "CTL accumulation term : %d".format(ctlTerm)
        text3.text = "PMC drawing term : %d".format(pmcTerm)


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
            setLabelCount(5, true)
            enableGridDashedLine(10f,10f,0f)
            setDrawZeroLine(true)
            spaceBottom = 0F
            granularity = 1F
        }

        // Y軸（右）
        chartArea1.axisRight.apply {
            setDrawLabels(true)
            setAxisMaxValue(1000F)
            setAxisMinValue(0F)
            setLabelCount(5, true)
            enableGridDashedLine(10f,10f,0f)
            setDrawZeroLine(true)
            spaceBottom = 0F
            granularity = 1F
        }

        chartArea1.apply {
            isClickable = false
            setDescription("")
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
        val ma: Activity = activity as Activity

        // PMC期間設定（例によって0:0:0セットが面倒くせえ）
        var begin = Calendar.getInstance()
        begin.add( Calendar.DAY_OF_MONTH, -1*(pmcTerm))
        var end = Calendar.getInstance()

        // PMC期間中のTSSリストを作る
        val realm = Realm.getInstance(runRealmConfig)

        val runs = realm.where<RunData>().between("date", begin.time, end.time).findAll().sort("date", Sort.DESCENDING)
        var tssList = IntArray( pmcTerm )

        for( r in runs) {
            var rd = Calendar.getInstance()
            rd.setTime(r.date)
            rd.set( Calendar.HOUR_OF_DAY, 0)
            rd.set( Calendar.MINUTE, 0)
            rd.set( Calendar.SECOND, 0)
            var diff = (rd.timeInMillis - begin.timeInMillis) / (1000*24*60*60)

            tssList[ diff.toInt() ] += r.tss
        }

        text4.text = "%d records in RunRealm".format(runs.size)



        // ATL/CTL/TSBを作る （ついでにＸ軸も）
        var atlList = FloatArray( pmcTerm )
        var ctlList = FloatArray( pmcTerm )
        var tsbList = FloatArray( pmcTerm )
        var xLabels = Array<String>( pmcTerm ){ "" }
        var bc = Calendar.getInstance()
        bc = begin
        bc.add( Calendar.DAY_OF_MONTH, 1 )

        for( i in 1..tssList.size-1) {
            atlList[i] = ( exp(-1.0 / atlTerm) * (atlList[i-1]-tssList[i]) + tssList[i] ).toFloat()
            ctlList[i] = ( exp(-1.0 / ctlTerm) * (ctlList[i-1]-tssList[i]) + tssList[i] ).toFloat()
            tsbList[i] = ctlList[i-1] - atlList[i-1]
        }

        // とりあえず適当に
        val xLabels2 = Array<String>( pmcTerm ) {it.toString()}
        val tssValues = arrayOfNulls<BarEntry>( pmcTerm )
        val atlValues = arrayOfNulls<Entry>( pmcTerm )
        val ctlValues = arrayOfNulls<Entry>( pmcTerm )
        val tsbValues = arrayOfNulls<Entry>( pmcTerm )

        for( i in 0..tssList.size-1 ) {
//            xLabels[i] = i.toString()
            xLabels[i] = "%2d/%2d".format(bc.get(Calendar.MONTH), bc.get(Calendar.DAY_OF_MONTH))
            bc.add( Calendar.DAY_OF_MONTH, 1)
            tssValues[i] = BarEntry( tssList[i].toFloat(), i )
            atlValues[i] = Entry( atlList[i], i )
            ctlValues[i] = Entry( ctlList[i], i )
            tsbValues[i] = Entry( tsbList[i], i )
        }

        val ctlLineDataSet = LineDataSet(ctlValues.toMutableList(), getString(R.string.pmc_ctl_name)).apply {
            setDrawValues(false)
            lineWidth = 4F
            color = ma.getColor(R.color.ctlBarColor)
            axisDependency = YAxis.AxisDependency.LEFT

//        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
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
        
        val tsbLineDataSet = LineDataSet(tsbValues.toMutableList(), getString(R.string.pmc_tsb_name)).apply {
            setDrawValues(false)
            setDrawCircles(false)
            lineWidth = 2F
            color = ma.getColor(R.color.tsbBarColor)
            axisDependency = YAxis.AxisDependency.LEFT
        }

        val atlLineDataSet = LineDataSet(atlValues.toMutableList(), getString(R.string.pmc_atl_name)).apply {
            setDrawValues(false)
            setDrawCircles(false)
            lineWidth = 2F
            color = ma.getColor(R.color.atlBarColor)
            axisDependency = YAxis.AxisDependency.LEFT
        }

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

