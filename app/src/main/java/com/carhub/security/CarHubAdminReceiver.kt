package com.carhub.security

import android.app.admin.DeviceAdminReceiver

/**
 * Device Admin / Device Owner receiver. Only becomes effective if Car Hub is
 * provisioned as Device Owner (Tier A). Harmless otherwise.
 */
class CarHubAdminReceiver : DeviceAdminReceiver()
