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

package me.dmdev.epicpredictor.domain.report

import me.dmdev.epicpredictor.domain.AgileRepository

class EpicReportInteractor(
    private val agileRepository: AgileRepository
) {

    suspend operator fun invoke(
        epicKeys: List<String>,
        sprintsCountForCalculation: SprintsCount,
        backlogGrowthRateFactor: BacklogGrowthRateFactor
    ) : Result<EpicReport> {

        val results = epicKeys.map { epicKey -> agileRepository.getEpicIssues(epicKey) }

        val anyError = results.find { it.isFailure }?.exceptionOrNull()

        return if (anyError != null) {
            Result.failure(anyError)
        } else {
            val epicReport = results
                .mapNotNull { it.getOrNull() }
                .flatten()
                .prepareEpicReport(
                    calculationSprintsCount = sprintsCountForCalculation,
                    backlogGrowthRateFactor = backlogGrowthRateFactor
                )
            Result.success(epicReport)
        }
    }
}
