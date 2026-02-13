package com.coxphysics.terrapins.views.utils

import com.coxphysics.terrapins.models.assessment.AssessmentResults
import com.coxphysics.terrapins.view_models.assessment.ReportVM
import com.coxphysics.terrapins.views.assessment.results.ReportView
import java.awt.Dimension

class Utils
{
    companion object
    {
        @JvmStatic
        fun run_results_viewer(results: AssessmentResults)
        {
            val report_view_model = ReportVM.from_results(results)
            val report_view = ReportView.from(report_view_model)
            report_view.preferredSize = Dimension(400, 400)
            report_view.pack()
            report_view.isVisible = true
        }
    }
}