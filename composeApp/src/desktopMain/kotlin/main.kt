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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.dmdev.epicpredictor.MainContainer
import me.dmdev.epicpredictor.presentation.MainPm
import me.dmdev.epicpredictor.Serializers
import me.dmdev.epicpredictor.ui.App
import me.dmdev.premo.JvmPmDelegate
import me.dmdev.premo.saver.JsonFileStateSaver
import java.awt.Dimension

fun main() {
    val pmDelegate = JvmPmDelegate<MainPm>(
        pmDescription = MainPm.Description,
        pmFactory = MainContainer(),
        pmStateSaver = JsonFileStateSaver(Serializers.json)
    )

    application {

        val windowState = rememberWindowState(width = 800.dp, height = 600.dp)
        pmDelegate.attachWindowLifecycle(windowState)

        Window(
            title = "Epic Predictor",
            state = windowState,
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(800, 600)
            App(pmDelegate.presentationModel)
        }
    }
}

@Composable
fun JvmPmDelegate<*>.attachWindowLifecycle(windowState: WindowState) {
    LaunchedEffect(this, windowState) {
        snapshotFlow(windowState::isMinimized).collect { isMinimized ->
            if (isMinimized) {
                onBackground()
            } else {
                onForeground()
            }
        }
    }

    DisposableEffect(this) {
        onCreate()
        onDispose(::onDestroy)
    }
}