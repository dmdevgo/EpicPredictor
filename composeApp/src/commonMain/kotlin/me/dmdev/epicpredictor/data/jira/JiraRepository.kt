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

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import me.dmdev.epicpredictor.domain.AgileRepository
import me.dmdev.epicpredictor.domain.Epic
import me.dmdev.epicpredictor.domain.Issue

class JiraRepository(
    private val httpClient: HttpClient
) : AgileRepository {

    override suspend fun getEpicIssues(epicIdOrKey: String): Result<List<Issue>> {
        return try {
            val response = httpClient.get("epic/$epicIdOrKey/issue") {
                url {
                    parameters.append("startAt", 0.toString())
                    parameters.append("maxResults", 1000.toString())
                    parameters.append(
                        name = "fields",
                        value = "sprint,closedSprints,status,created,resolutiondate"
                    )
                }
            }.body<EpicIssuesResponse>()

            Result.success(response.issues.map { it.toIssue() })
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun getEpic(epicIdOrKey: String): Result<Epic> {
        return try {
            val epic = httpClient.get("epic/$epicIdOrKey").body<EpicDto>().toEpic()
            Result.success(epic)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
