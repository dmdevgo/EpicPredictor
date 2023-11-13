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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.dmdev.epicpredictor.domain.Sprint

@Serializable
data class SprintDto(
    @SerialName("id")
    val id: Int,

    @SerialName("name")
    val name: String,

    @SerialName("state")
    val state: String,

    @SerialName("startDate")
    val startDate: String? = null,

    @SerialName("endDate")
    val endDate: String? = null,

    @SerialName("completeDate")
    val completeDate: String? = null,
)

fun SprintDto.toSprint() : Sprint {
    return Sprint(
        id = id,
        name = name,
        state = when (state) {
            "active" -> Sprint.State.ACTIVE
            "closed" -> Sprint.State.CLOSED
            else -> Sprint.State.FUTURE
        },
        startDate = if (startDate != null) Instant.parse(startDate) else null,
        endDate = if (endDate != null) Instant.parse(endDate) else null,
        completeDate = if (completeDate != null) Instant.parse(completeDate) else null,
    )
}
