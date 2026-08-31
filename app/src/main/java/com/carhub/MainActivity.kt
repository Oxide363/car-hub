package com.carhub

import android.media.AudioManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carhub.security.Kiosk
import com.carhub.ui.CarHubShell
import com.carhub.ui.CarHubTheme
import com.carhub.ui.PinEntryScreen
import com.carhub.ui.PinSetupScreen
import com.carhub.ui.SplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Stay awake in the car; route hardware volume keys to media stream.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        volumeControlStream = AudioManager.STREAM_MUSIC

        setContent {
            CarHubTheme {
                val vm: MainViewModel = viewModel()
                when {
                    !vm.loaded -> SplashScreen()
                    !vm.hasPin -> PinSetupScreen(onDone = { vm.setupPin(it) })
                    else -> {
                        LaunchedEffect(vm.mode) {
                            if (vm.mode == Mode.PASSENGER) Kiosk.startLock(this@MainActivity)
                            else Kiosk.stopLock(this@MainActivity)
                        }
                        BackHandler {
                            when {
                                vm.askExitPin -> vm.askExitPin = false
                                vm.playing != null -> vm.playing = null
                                vm.section != Section.HOME -> vm.go(Section.HOME)
                                vm.mode == Mode.OWNER -> finish()
                                else -> { /* passenger at Home: stay put */ }
                            }
                        }
                        if (vm.askExitPin) {
                            PinEntryScreen(
                                title = "Enter Owner PIN to exit",
                                onVerify = { vm.verifyPin(it) },
                                onSuccess = { vm.exitPassenger() },
                                onCancel = { vm.askExitPin = false }
                            )
                        } else {
                            CarHubShell(vm)
                        }
                    }
                }
            }
        }
    }
}
