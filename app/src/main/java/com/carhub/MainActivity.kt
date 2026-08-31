package com.carhub

import android.Manifest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carhub.security.Kiosk
import com.carhub.ui.CarHubShell
import com.carhub.ui.CarHubTheme
import com.carhub.ui.ExitGate
import com.carhub.ui.PinSetupScreen
import com.carhub.ui.SplashScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Stay awake in the car; route hardware volume keys to media stream.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Best-effort: needed only to show Bluetooth status in the cluster.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1
                )
            } catch (e: Exception) { }
        }

        setContent {
            CarHubTheme {
                val vm: MainViewModel = viewModel()
                when {
                    !vm.loaded -> SplashScreen()
                    !vm.hasPin || !vm.hasPattern -> PinSetupScreen(
                        onComplete = { pin, pattern -> vm.setupPin(pin); vm.setupPattern(pattern) }
                    )
                    else -> {
                        LaunchedEffect(vm.mode) {
                            if (vm.mode == Mode.PASSENGER) Kiosk.startLock(this@MainActivity)
                            else Kiosk.stopLock(this@MainActivity)
                        }
                        LaunchedEffect(vm.brightness) {
                            val lp = window.attributes
                            lp.screenBrightness =
                                if (vm.brightness < 0f) WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                                else vm.brightness
                            window.attributes = lp
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
                            ExitGate(
                                lockedSeconds = vm.lockRemainingSec(),
                                verifyPin = { vm.verifyPin(it) },
                                verifyPattern = { vm.verifyPattern(it) },
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
