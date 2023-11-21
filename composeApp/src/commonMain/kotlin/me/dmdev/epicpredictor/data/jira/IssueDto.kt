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

package me.dmdev.epicpredictor.data.jira

import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.dmdev.epicpredictor.domain.Issue

@Serializable
data class IssueDto(
    @SerialName("id")
    val id: String,

    @SerialName("key")
    val key: String,

    @SerialName("fields")
    val fields: Fields? = null
) {

    @Serializable
    data class Fields(
        @SerialName("sprint")
        val sprint: SprintDto? = null,

        @SerialName("status")
        val status: StatusDto,

        @SerialName("created")
        val createdDate: String,

        @SerialName("resolutiondate")
        val resolutionDate: String? = null,

        @SerialName("closedSprints")
        val closedSprints: List<SprintDto> = listOf()
    )

    @Serializable
    data class StatusDto(
        @SerialName("name")
        val name: String,
    )
}

fun IssueDto.toIssue(): Issue {
    return Issue(
        id = id,
        key = key,
        status = when (fields?.status?.name) {
            "Closed" -> Issue.Status.CLOSED
            else -> Issue.Status.ANY
        },
        sprint = fields?.sprint?.toSprint(),
        createdDate = fields?.createdDate?.take(26)?.toInstant()
            ?: Instant.fromEpochMilliseconds(0),
        resolutionDate = fields?.resolutionDate?.take(26)?.toInstant(),
        closedSprints = fields?.closedSprints?.map { it.toSprint() } ?: listOf()
    )
}
