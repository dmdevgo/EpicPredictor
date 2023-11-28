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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.dmdev.epicpredictor.presentation.MainPm

@Composable
internal fun App(pm: MainPm) {
    val state = pm.stateFlow.collectAsState().value

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.inProgress) {
            CircularProgressIndicator(color = Colors.open)
        } else {
            if (state.error != null) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = state.error
                    )
                    ReportButton(
                        text = "Retry",
                        enabled = true
                    ) {
                        pm.onRetryClick()
                    }
                }
            } else if (state.epicReport != null) {
                EpicReportView(
                    epicReport = state.epicReport,
                    onNewReportClick = pm::onNewReport
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Epics For Calculations:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Column {
                        state.epicItems.forEach { epicItem ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = epicItem.checked,
                                    onCheckedChange = { pm.onEpicItemClick(epicItem) }
                                )
                                Text(epicItem.epic.name)
                            }
                        }
                    }

                    SprintsCountMenuView(
                        selectedItem = state.sprintsCountItem,
                        dialogNavigation = pm.sprintsCountMenuDialog,
                        onExpand = { pm.onSprintsCountClick() }
                    )

                    BacklogGrowthRateFactorMenuView(
                        selectedItem = state.backlogGrowthRateFactorItem,
                        dialogNavigation = pm.backlogGrowthRateFactorMenuDialog,
                        onExpand = { pm.onBacklogGrowthRateFactorClick() }
                    )

                    ReportButton(
                        text = "Report",
                        enabled = state.reportButtonEnabled
                    ) {
                        pm.onReportClick()
                    }
                }
            }
        }
    }
}
