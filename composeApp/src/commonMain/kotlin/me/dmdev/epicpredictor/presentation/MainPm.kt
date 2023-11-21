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
import me.dmdev.epicpredictor.domain.AgileRepository
import me.dmdev.epicpredictor.domain.report.EpicReport
import me.dmdev.epicpredictor.domain.report.prepareEpicReport
import me.dmdev.premo.PmDescription
import me.dmdev.premo.PmParams

class MainPm(
    val agileRepository: AgileRepository,
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
        val inProgress: Boolean = false
    )

    fun prepareReport() = runWithProgress {

        val results = BuildConfig.JIRA_EPIC_KEYS
            .split(",")
            .map { epicKey -> agileRepository.getEpicIssues(epicKey) }

        val anyFailResult = results.find { it.isFailure }

        if (anyFailResult == null) {
            val issues = results.mapNotNull { it.getOrNull() }.flatten()
            val epicReport = issues.prepareEpicReport()
            changeState { copy(epicReport = epicReport) }
        } else {
            val message = anyFailResult.exceptionOrNull()?.message ?: "Unknown error"
            changeState { copy(error = message) }
        }
    }

    private fun runWithProgress(
        block: suspend () -> Unit
    ) {
        scope.launch {
            changeState { copy(inProgress = true) }
            block.invoke()
            changeState { copy(inProgress = false) }
        }
    }
}
