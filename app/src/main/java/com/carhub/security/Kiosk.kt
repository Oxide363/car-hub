package com.carhub.security

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context

/**
 * Kiosk / lockdown helper.
 *
 * Tier A: Car Hub is provisioned as Device Owner -> full Lock Task (silent, escape-proof).
 * Tier B: Not Device Owner -> startLockTask() triggers user-confirmed screen pinning.
 *
 * The app never claims stronger lockdown than the device actually provides.
 */
object Kiosk {

    private fun dpm(context: Context): DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private fun admin(context: Context): ComponentName =
        ComponentName(context, CarHubAdminReceiver::class.java)

    fun isDeviceOwner(context: Context): Boolean =
        try { dpm(context).isDeviceOwnerApp(context.packageName) } catch (e: Exception) { false }

    /** "A" = full Device Owner lockdown, "B" = screen pinning only. */
    fun tier(context: Context): String = if (isDeviceOwner(context)) "A" else "B"

    fun startLock(activity: Activity) {
        try {
            val d = dpm(activity)
            if (d.isDeviceOwnerApp(activity.packageName)) {
                d.setLockTaskPackages(admin(activity), arrayOf(activity.packageName))
            }
            activity.startLockTask()
        } catch (e: Exception) {
            // Screen pinning may require user confirmation or be unavailable; ignore safely.
        }
    }

    fun stopLock(activity: Activity) {
        try { activity.stopLockTask() } catch (e: Exception) { }
    }
}
