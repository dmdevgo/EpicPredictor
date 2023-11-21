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

import me.dmdev.epicpredictor.domain.Issue
import me.dmdev.epicpredictor.domain.Sprint
import me.dmdev.epicpredictor.domain.closedBeforeOrIn
import me.dmdev.epicpredictor.domain.committedIn
import me.dmdev.epicpredictor.domain.completedIn
import me.dmdev.epicpredictor.domain.createdBeforeOrIn
import me.dmdev.epicpredictor.domain.createdIn
import me.dmdev.epicpredictor.domain.isClosed
import me.dmdev.epicpredictor.domain.isClosedOrActive

data class EpicReport(
    val sprintReports: List<SprintReport>,
    val issuesReport: IssuesReport,
    val velocityReport: VelocityReport,
    val burndownReports: List<SprintReport>,
    val lastSprintsCountForCalculation: Int,
    val backlogGrowthRateFactor: Double
)

fun List<Issue>.prepareEpicReport(
    lastSprintsCountForCalculation: Int = 6,
    backlogGrowthRateFactor: Double = 0.2 // 20 percent of velocity
): EpicReport {

    val issuesReport = prepareIssuesReport()
    val sprintReports = prepareSprintReports()
    val velocityReport = sprintReports.prepareVelocityReport(lastSprintsCountForCalculation)

    val burndownReport = issuesReport.prepareBurndownReport(
        velocityReport = velocityReport,
        backlogGrowthRateFactor = backlogGrowthRateFactor
    )

    return EpicReport(
        sprintReports = sprintReports,
        issuesReport = issuesReport,
        velocityReport = velocityReport,
        burndownReports = sprintReports.plus(burndownReport),
        lastSprintsCountForCalculation = lastSprintsCountForCalculation,
        backlogGrowthRateFactor = backlogGrowthRateFactor
    )
}

private fun IssuesReport.prepareBurndownReport(
    velocityReport: VelocityReport,
    backlogGrowthRateFactor: Double
): List<SprintReport> {

    if (velocityReport.burndown <= 0.0) return listOf()

    val remainingSprints = mutableListOf<SprintReport>()

    var totalCount = totalCount.toDouble()
    var closedCount = closedCount.toDouble()

    var number = 1

    while (closedCount < totalCount) {
        closedCount += velocityReport.burndown
        totalCount += velocityReport.burndown * backlogGrowthRateFactor

        if (closedCount > totalCount) {
            closedCount = totalCount
        }

        remainingSprints.add(
            SprintReport(
                sprintName = number++.toString(),
                committedIssues = velocityReport.burndown.toInt(),
                completedIssues = velocityReport.burndown.toInt(),
                createdIssues = 0,
                totalIssues = totalCount.toInt(),
                totalClosedIssues = closedCount.toInt(),
                isClosed = false
            )
        )
    }

    return remainingSprints
}

private fun List<Issue>.prepareIssuesReport(): IssuesReport {
    return IssuesReport(
        totalCount = size,
        closedCount = filter { it.isClosed }.size,
        openCount = filter { it.isClosed.not() }.size
    )
}

private fun List<SprintReport>.prepareVelocityReport(lastSprintCount: Int): VelocityReport {

    val sprints = this.filter { it.isClosed }.takeLast(lastSprintCount)

    val burndownVelocity = sprints
        .sumOf { it.completedIssues }
        .div(sprints.size.toDouble())

    val backlogVelocity = sprints
        .sumOf { it.createdIssues }
        .div(sprints.size.toDouble())

    return VelocityReport(
        burndown = burndownVelocity,
        backlog = backlogVelocity,
    )
}

private fun List<Issue>.prepareSprintReports(): List<SprintReport> {

    val issues = this

    return issues
        .fold(mutableSetOf<Sprint>()) { sprints, issue ->
            if (issue.sprint != null) {
                sprints.add(issue.sprint)
            }
            issue.closedSprints.forEach { sprint ->
                sprints.add(sprint)
            }
            sprints
        }
        .filter { it.isClosedOrActive() }
        .sortedBy { it.startDate }
        .map { sprint ->

            val committedIssues = issues.filter { it.committedIn(sprint) }
            val completedIssues = issues.filter { it.completedIn(sprint) }
            val createdIssues = issues.filter { it.createdIn(sprint) }
            val totalIssues = issues.filter { it.createdBeforeOrIn(sprint) }
            val totalClosedIssues = issues.filter { it.closedBeforeOrIn(sprint)}

            SprintReport(
                sprintName = sprint.name,
                committedIssues = committedIssues.size,
                completedIssues = completedIssues.size,
                createdIssues = createdIssues.size,
                totalIssues = totalIssues.size,
                totalClosedIssues = totalClosedIssues.size,
                isClosed = sprint.isClosed()
            )
        }
}
