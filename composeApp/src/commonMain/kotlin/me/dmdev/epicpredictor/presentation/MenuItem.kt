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

import me.dmdev.epicpredictor.domain.report.BacklogGrowthRateFactor
import me.dmdev.epicpredictor.domain.report.SprintsCount

data class MenuItem<T>(
    val name: String,
    val value: T
)

fun BacklogGrowthRateFactor.toMenuItem(): MenuItem<BacklogGrowthRateFactor> {
    return when(this) {
        BacklogGrowthRateFactor.Half -> MenuItem("50 percent of velocity", this)
        BacklogGrowthRateFactor.OneFifth -> MenuItem("20 percent of velocity", this)
        BacklogGrowthRateFactor.OneThird -> MenuItem("33 percent of velocity", this)
        BacklogGrowthRateFactor.Zero -> MenuItem("No backlog growth", this)
    }
}

fun SprintsCount.toMenuItem(): MenuItem<SprintsCount> {
    return when (this) {
        SprintsCount.ALL -> MenuItem("All sprints", this)
        SprintsCount.LastSix -> MenuItem("Last six sprints", this)
        SprintsCount.LastThree -> MenuItem("Last three sprints", this)
    }
}
