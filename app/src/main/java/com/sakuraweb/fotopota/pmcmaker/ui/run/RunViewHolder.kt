package com.sakuraweb.fotopota.pmcmaker.ui.run

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.one_run_card.view.*

class RunViewHolder(iv: View) : RecyclerView.ViewHolder(iv) {
    var dateText:   TextView? = null
    var termText:   TextView? = null
    var tssText:    TextView? = null
    var kmText:     TextView? = null
    var kcalText:   TextView? = null
    var placeText:  TextView? = null
    var memoText:   TextView? = null

    init {
        dateText    = iv.oneRunDate
        termText    = iv.oneRunDuration
        tssText     = iv.oneRunTSS
        kmText      = iv.oneRunKm
        kcalText    = iv.oneRunKcal
        placeText   = iv.oneRunPlace
        memoText    = iv.oneRunMemo
    }
}
