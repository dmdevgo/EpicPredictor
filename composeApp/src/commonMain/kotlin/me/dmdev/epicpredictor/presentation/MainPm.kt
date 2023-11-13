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
import me.dmdev.epicpredictor.domain.Issue
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
        val error: String? = null
    )

    data class EpicReport(
        val totalIssuesCount: Int,
        val closedIssuesCount: Int,
        val openIssuesCount: Int,
    )

    init {
        scope.launch {
            val result = agileRepository.getEpicIssues(BuildConfig.JIRA_EPIC_ID)
            if (result.isSuccess) {
                val issues = result.getOrNull() ?: listOf()
                changeState { copy(epicReport = prepareEpicReport(issues)) }
            } else {
                val message = result.exceptionOrNull()?.message
                println("Error = $message")
                changeState { copy(error = message) }
            }
        }
    }

    private fun prepareEpicReport(issues: List<Issue>): EpicReport {
        return EpicReport(
            totalIssuesCount = issues.size,
            closedIssuesCount = issues.filter { it.isClosed }.size,
            openIssuesCount = issues.filter { it.isClosed.not() }.size,
        )
    }
}
