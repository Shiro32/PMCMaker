package com.sakuraweb.fotopota.pmcmaker.ui.pmc

import com.sakuraweb.fotopota.pmcmaker.R


class DiagDrawData (
    var colorID: Int,
    var titleID: Int,
    var subTitleID: Int,
    var descriptionID: Int,
    var unitID: Int,
    var iconID: Int,
    var score: String,
    var messageID: Int )
{}

fun drawDIAG1() : DiagDrawData {
    // TSBが-50以下になっていたらCaution2
    // １週間平均が-50以下ならCaution1
    var icon: Int = R.drawable.safety2
    var color: Int = R.color.diagNoProblem
    var judge: Int = R.string.diag_1_ok

    when {
        (revTSBs[0] < -50) -> {
            icon = R.drawable.caution2
            color = R.color.diagBad
            judge = R.string.diag_1_ng
        }
//        (revTSBs.drop(7).average() < -50) -> {
//            icon = R.drawable.caution
//            judge = R.string.diag_1_ng
//        }
    }

    return DiagDrawData(
        color, R.string.diag_1_title_label, R.string.diag_1_subtitle_label, R.string.diag_1_description, R.string.diag_1_unit,
        icon, revTSBs[0].toInt().toString(), judge)
}


fun drawDIAG2() : DiagDrawData {
    // １０日に１回以上、TSBが-20以下でCaution1
    var icon = R.drawable.safety2
    var color = R.color.diagNoProblem
    var judge = R.string.diag_2_ok

    val count = revTSBs.take(10).count {it <= -20}
    if( count >= 1 ) {
        icon = R.drawable.caution
        color = R.color.diagBad
        judge = R.string.diag_2_ng
    }

    return DiagDrawData(
        color, R.string.diag_2_title_label, R.string.diag_2_subtitle_label, R.string.diag_2_description, R.string.diag_2_unit,
        icon, count.toString(), judge)
}

fun drawDIAG3() : DiagDrawData {
    // +5くらいのTSBがレースに最適
    var icon = R.drawable.growth
    var color = R.color.diagGood
    var msg = R.string.diag_3_ok
    val tsb = revTSBs[0].toInt()

    if(tsb !in 3..7) {
        icon = R.drawable.caution
        color = R.color.diagBad
        msg = R.string.diag_3_ng
    }

    return DiagDrawData(
        color, R.string.diag_3_title_label, R.string.diag_3_subtitle_label, R.string.diag_3_description, R.string.diag_3_unit,
        icon, tsb.toString(), msg)
}

fun drawDIAG4() : DiagDrawData {
    // CTL=50を目標にするとよいようです。
    var icon = R.drawable.growth
    var color = R.color.diagGood
    var msg = R.string.diag_4_ok

    if( revCTLs[0]<50 ) {
        icon = R.drawable.bike
        color = R.color.diagBad
        msg = R.string.diag_4_ng
    }

    return DiagDrawData(
        color, R.string.diag_4_title_label, R.string.diag_4_subtitle_label, R.string.diag_4_description, R.string.diag_4_unit,
        icon, revCTLs[0].toInt().toString(), msg)
}

fun drawDIAG5() : DiagDrawData {
    // １週間で５程度、CTLが上昇するとよい
    var icon = R.drawable.growth
    var color = R.color.diagGood
    var msg = R.string.diag_5_ok
    val growth = if( revCTLs.size>=7) ( revCTLs[0] - revCTLs[6] ).toInt() else 0

    when {
        (growth < 4) -> {
            icon = R.drawable.caution2
            color = R.color.diagBad
            msg = R.string.diag_5_ng_under
        }
        (growth > 6) -> {
            icon = R.drawable.caution2
            color = R.color.diagBad
            msg = R.string.diag_5_ng_over
        }
    }

    return DiagDrawData(
        color, R.string.diag_5_title_label, R.string.diag_5_subtitle_label, R.string.diag_5_description, R.string.diag_5_unit,
        icon, growth.toString(), msg)
}

fun drawDIAG6() : DiagDrawData {
    // １週間でに7TSS以上上がる状態が４週間続くならオーバートレーニング
    // （１週間前 - 今週）＞７　を２８日間やるということ？
    var icon = R.drawable.safety2
    var color = R.color.diagNoProblem
    var msg = R.string.diag_6_ok
    var count = 0

    // 一応、bounceチェックしながら、4週間分の増加スピード測定
    val t1 = if( revCTLs.size>= 7 ) revCTLs[ 0] - revCTLs[ 6] else 0F
    val t2 = if( revCTLs.size>=14 ) revCTLs[ 7] - revCTLs[13] else 0F
    val t3 = if( revCTLs.size>=21 ) revCTLs[14] - revCTLs[20] else 0F
    val t4 = if( revCTLs.size>=28 ) revCTLs[21] - revCTLs[27] else 0F

    if( t1>=7f ) count++
    if( t2>=7f ) count++
    if( t3>=7f ) count++
    if( t4>=7f ) count++

    if( count==4 ) {
        icon = R.drawable.caution2
        color = R.color.diagBad
        msg  = R.string.diag_6_ng
    }

    return DiagDrawData(
        color, R.string.diag_6_title_label, R.string.diag_6_subtitle_label, R.string.diag_6_description, R.string.diag_6_unit,
        icon, count.toString(), msg)
}

fun drawDIAG7() : DiagDrawData {
    // 2週連続でCTLが減少するとサボりすぎ
    var icon = R.drawable.bike
    var msg = R.string.diag_7_ok
    var color = R.color.diagGood
    var count = 0

    // 一応、bounceチェックしながら、2週間分の増加スピード測定
    val t1 = if( revCTLs.size>= 7 ) revCTLs[ 0] - revCTLs[ 6] else 0F
    val t2 = if( revCTLs.size>=14 ) revCTLs[ 7] - revCTLs[13] else 0F

    if( t1<=0F ) count++
    if( t2<=0f ) count++

    if( count==2 ) {
        icon = R.drawable.caution2
        color = R.color.diagBad
        msg  = R.string.diag_7_ng
    }

    return DiagDrawData(
        color, R.string.diag_7_title_label, R.string.diag_7_subtitle_label, R.string.diag_7_description, R.string.diag_7_unit,
        icon, count.toString(), msg)
}

fun drawDIAG8() : DiagDrawData {
    // 1週間でATLが70TSSも増えたら危ない！
    var icon = R.drawable.safety2
    var color = R.color.diagNoProblem
    var msg = R.string.diag_8_ok
    var atl = 0

    if( revATLs.size >= 7 ) {
        atl = (revATLs[0] - revATLs[6]).toInt()
        if( revATLs[0]- revATLs[6] >= 70 ) {
            icon = R.drawable.caution2
            color = R.color.diagBad
            msg = R.string.diag_8_ng
        }
    }

    return DiagDrawData(
        color, R.string.diag_8_title_label, R.string.diag_8_subtitle_label, R.string.diag_8_description, R.string.diag_8_unit,
        icon, atl.toString(), msg)
}
