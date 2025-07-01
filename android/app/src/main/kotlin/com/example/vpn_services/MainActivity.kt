package com.example.vpn_services

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.provider.Settings
import android.content.Intent
import android.util.Log
import android.text.TextUtils
import android.app.admin.DevicePolicyManager
import android.content.ComponentName

class MainActivity: FlutterActivity() {
    private val CHANNEL = "vpn_services/channel"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            when (call.method) {
                "startVpn" -> {
                    val intent = Intent(this, MyVpnService::class.java)
                    startService(intent)
                    result.success("VPN started")
                }
                "blockWebsite" -> {
                    val domain = call.argument<String>("domain")
                    // TODO: Pass this domain to your VPN service (e.g., via a static list, shared prefs, or IPC)
                    Log.d("MainActivity", "Block request for: $domain")
                    result.success("Requested to block $domain")
                }
                "requestAccessibilityPermission" -> {
                    if (!isAccessibilityServiceEnabled()) {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    }
                    result.success(null)
                }
                "requestDeviceAdmin" -> {
                    val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This app needs device admin access.")
                    startActivity(intent)
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    // Helper function to check if your accessibility service is enabled
    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = "$packageName/.YourAccessibilityService"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabledServices != null && enabledServices.contains(expectedComponentName)
    }
}
