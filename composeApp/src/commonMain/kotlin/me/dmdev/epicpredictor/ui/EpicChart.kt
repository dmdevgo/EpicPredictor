/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Dmitriy Gorbunov (dmitriy.goto@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.dmdev.epicpredictor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aay.compose.donutChart.DonutChart
import com.aay.compose.donutChart.model.PieChartData
import me.dmdev.epicpredictor.domain.report.EpicReport

@Composable
fun EpicChart(
    report: EpicReport,
    onNewReportClick: () -> Unit
) {

    val pieChartData = remember(report.issuesReport) {
        listOf(
            PieChartData(
                partName = "Open",
                data = report.issuesReport.openCount.toDouble(),
                color = Colors.open,
            ),
            PieChartData(
                partName = "Closed",
                data = report.issuesReport.closedCount.toDouble(),
                color = Colors.closed,
            ),
        )
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DonutChart(
            modifier = Modifier.size(600.dp, 600.dp),
            pieChartData = pieChartData,
            centerTitle = "Issues",
            outerCircularColor = Color.LightGray,
            innerCircularColor = Color.Gray,
            ratioLineColor = Color.LightGray,
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.padding(16.dp),
            ) {
                Column(
                    modifier = Modifier.wrapContentSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Total Issues: ${report.issuesReport.totalCount}")
                    Text("Closed Issues: ${report.issuesReport.closedCount}")
                    Text("Open Issues: ${report.issuesReport.openCount}")
                    Text("Sprints: ${report.sprintReports.size}")
                    Text("Remaining Sprints: ${report.burndownReports.size - report.sprintReports.size}")
                    Text("Velocity: ${report.velocityReport.burndown}")
                    Text("Backlog Velocity: ${report.velocityReport.backlog}")
                    Text("Last Sprints Count For Calculation: ${report.lastSprintsCountForCalculation}")
                    Text("Backlog Growth Rate Factor: ${report.backlogGrowthRateFactor.value}")
                }
            }
            ReportButton(
                text = "New Report",
                onClick = onNewReportClick
            )
        }
    }
}