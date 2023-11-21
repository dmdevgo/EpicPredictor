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

package me.dmdev.epicpredictor.domain

import kotlinx.datetime.Instant

data class Issue(
    val id: String,
    val key: String,
    val status: Status,
    val createdDate: Instant,
    val resolutionDate: Instant?,
    val sprint: Sprint?,
    val closedSprints: List<Sprint>
) {

    val isClosed: Boolean get() = status == Status.CLOSED

    enum class Status {
        ANY,
        CLOSED
    }
}

fun Issue.committedIn(sprint: Sprint): Boolean {
    return closedSprints.contains(sprint) || this.sprint == sprint
}

fun Issue.completedIn(sprint: Sprint): Boolean {
    val lastClosedSprint = closedSprints.maxByOrNull {
        it.endDate?.toEpochMilliseconds() ?: 0
    }
    return isClosed && (lastClosedSprint == sprint || this.sprint == sprint)
}

fun Issue.createdIn(sprint: Sprint): Boolean {
    if (sprint.startDate == null || sprint.endDate == null) return false

    return createdDate.toEpochMilliseconds() >= sprint.startDate.toEpochMilliseconds()
                && createdDate.toEpochMilliseconds() <= sprint.endDate.toEpochMilliseconds()
}

fun Issue.createdBeforeOrIn(sprint: Sprint): Boolean {
    if (sprint.endDate == null) return true

    return createdDate.toEpochMilliseconds() <= sprint.endDate.toEpochMilliseconds()
}

fun Issue.closedBeforeOrIn(sprint: Sprint): Boolean {
    if (sprint.endDate == null || resolutionDate == null) return false

    return resolutionDate.toEpochMilliseconds() <= sprint.endDate.toEpochMilliseconds()
}
