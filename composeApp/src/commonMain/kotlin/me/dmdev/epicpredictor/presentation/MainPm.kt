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

package me.dmdev.epicpredictor.presentation

import Epic_Predictor.composeApp.BuildConfig
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.dmdev.epicpredictor.domain.GetEpicsInteractor
import me.dmdev.epicpredictor.domain.report.BacklogGrowthRateFactor
import me.dmdev.epicpredictor.domain.report.EpicReport
import me.dmdev.epicpredictor.domain.report.EpicReportInteractor
import me.dmdev.epicpredictor.domain.report.SprintsCount
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams
import me.dmdev.premo.navigation.DialogNavigator

class MainPm(
    private val getEpicsInteractor: GetEpicsInteractor,
    private val epicReportInteractor: EpicReportInteractor,
    params: PmParams
) : SingleStatePm<MainPm.State>(
    initialState = State(),
    pmParams = params
) {

    @Serializable
    object Description : PmDescription

    data class State(
        val epicReport: EpicReport? = null,
        val error: String? = null,
        val loadEpicsInProgress: Boolean = false,
        val calculateReportInProgress: Boolean = false,
        val epicItems: List<EpicItem> = listOf(),
        val sprintsCountItem: MenuItem<SprintsCount> =
            SprintsCount.LastSix.toMenuItem(),
        val backlogGrowthRateFactorItem: MenuItem<BacklogGrowthRateFactor> =
            BacklogGrowthRateFactor.OneFifth.toMenuItem(),
    ) {
        val inProgress = loadEpicsInProgress || calculateReportInProgress
        val reportButtonEnabled: Boolean = epicItems.any { it.checked }
    }

    val sprintsCountMenuDialog =
        DialogNavigator<SprintsCountMenuPm, AbstractMenuPm<SprintsCount>.ResultMessage>(
            key = "sprints_count",
            onDismissRequest = {
                it.dismiss()
            }
        ) { message ->
            message?.let {
                state = state.copy(
                    sprintsCountItem = message.item
                )
            }
        }

    val backlogGrowthRateFactorMenuDialog =
        DialogNavigator<BacklogGrowthRateFactorMenuPm, AbstractMenuPm<BacklogGrowthRateFactor>.ResultMessage>(
            key = "backlog_growth_rate_factor"
        ) { message ->
            message?.let {
                state = state.copy(
                    backlogGrowthRateFactorItem = message.item
                )
            }
        }

    init {
        loadEpics()
    }

    fun onReportClick() {
        scope.launch {
            if (state.calculateReportInProgress) return@launch

            state = state.copy(calculateReportInProgress = true)

            val epicKeys = state.epicItems
                .filter { it.checked }
                .map { it.epic.id.toString() }

            val result = epicReportInteractor.invoke(
                epicKeys = epicKeys,
                sprintsCountForCalculation = state.sprintsCountItem.value,
                backlogGrowthRateFactor = state.backlogGrowthRateFactorItem.value,
            )

            state = state.copy(
                calculateReportInProgress = false,
                epicReport = result.getOrNull(),
                error = result.exceptionOrNull()?.message,
            )
        }
    }

    fun onRetryClick() {
        if (state.epicItems.isEmpty()) {
            loadEpics()
        } else {
            onReportClick()
        }
    }

    fun onNewReport() {
        state = state.copy(
            epicReport = null,
            error = null
        )
    }

    fun onSprintsCountClick() {
        sprintsCountMenuDialog.show(
            Child(SprintsCountMenuPm.Description)
        )
    }

    fun onBacklogGrowthRateFactorClick() {
        backlogGrowthRateFactorMenuDialog.show(
            Child(BacklogGrowthRateFactorMenuPm.Description)
        )
    }

    fun onEpicItemClick(epicItem: EpicItem) {
        val newEpicItems = state.epicItems.map { item ->
            if (epicItem == item) {
                item.copy(checked = item.checked.not())
            } else {
                item
            }
        }
        state = state.copy(
            epicItems = newEpicItems
        )
    }

    private fun loadEpics() {
        scope.launch {
            state = state.copy(loadEpicsInProgress = true)
            val result = getEpicsInteractor.invoke(getEpicKeys())
            val epicItems = result.getOrNull()?.map { epic ->
                EpicItem(
                    epic = epic,
                    checked = true
                )
            } ?: listOf()
            state = state.copy(
                loadEpicsInProgress = false,
                epicItems = epicItems,
                error = result.exceptionOrNull()?.message,
            )
        }
    }

    private fun getEpicKeys(): List<String> {
        return BuildConfig.JIRA_EPIC_KEYS.split(",")
    }
}
