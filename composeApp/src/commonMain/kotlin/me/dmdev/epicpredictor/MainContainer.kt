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

package me.dmdev.epicpredictor

import Epic_Predictor.composeApp.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.dmdev.epicpredictor.data.jira.JiraRepository
import me.dmdev.epicpredictor.domain.AgileRepository
import me.dmdev.epicpredictor.domain.GetEpicsInteractor
import me.dmdev.epicpredictor.domain.report.EpicReportInteractor
import me.dmdev.epicpredictor.presentation.BacklogGrowthRateFactorMenuPm
import me.dmdev.epicpredictor.presentation.MainPm
import me.dmdev.epicpredictor.presentation.SprintsCountMenuPm
import me.dmdev.premo.PmFactory
import me.dmdev.premo.PmParams
import me.dmdev.premo.PresentationModel

class MainContainer : PmFactory {

    override fun createPm(params: PmParams): PresentationModel {
        return when (val description = params.description) {
            is MainPm.Description -> createMainPm(params)
            is SprintsCountMenuPm.Description -> createSprintsCountMenuPm(params)
            is BacklogGrowthRateFactorMenuPm.Description -> createBacklogGrowthRateFactorMenuPm(params)
            else -> throw IllegalArgumentException(
                "Not handled instance creation for pm description: $description"
            )
        }
    }

    private fun createMainPm(params: PmParams): MainPm {
        return MainPm(
            getEpicsInteractor = GetEpicsInteractor(agileRepository),
            epicReportInteractor = EpicReportInteractor(agileRepository),
            params = params
        )
    }

    private fun createSprintsCountMenuPm(params: PmParams): SprintsCountMenuPm {
        return SprintsCountMenuPm(params = params)
    }

    private fun createBacklogGrowthRateFactorMenuPm(params: PmParams): BacklogGrowthRateFactorMenuPm {
        return BacklogGrowthRateFactorMenuPm(params = params)
    }

    private val agileRepository: AgileRepository by lazy {
        JiraRepository(httpClient)
    }

    private val httpClient: HttpClient by lazy {
        HttpClient {
            install(UserAgent) {
                agent = "Epic Predictor Desktop Client"
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 3)
                exponentialDelay()
            }
            install(ContentNegotiation) {
                json(
                    json = Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(HttpCache)
            install(HttpTimeout)
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                level = LogLevel.ALL
            }

            defaultRequest {
                url(BuildConfig.JIRA_BASE_URL + "rest/agile/1.0/")
                header(HttpHeaders.Authorization, "Bearer ${BuildConfig.JIRA_PERSONAL_ACCESS_TOKEN}")
            }
        }
    }
}