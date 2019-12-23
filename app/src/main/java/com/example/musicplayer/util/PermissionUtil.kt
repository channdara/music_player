package com.example.musicplayer.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

class PermissionUtil {
    companion object {
        private const val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
        private const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
        private const val GRANTED = PackageManager.PERMISSION_GRANTED

        fun allPermissionsGranted(context: Context): Boolean {
            val permPhoneState = ContextCompat.checkSelfPermission(context, READ_PHONE_STATE)
            val permStorage = ContextCompat.checkSelfPermission(context, READ_EXTERNAL_STORAGE)
            return (permPhoneState == GRANTED) && (permStorage == GRANTED)
        }

        fun requestPermissions(context: Context) {
            val activity = context as Activity
            if (allPermissionsGranted(context)) return
            activity.requestPermissions(arrayOf(READ_PHONE_STATE, READ_EXTERNAL_STORAGE), 1)
        }
    }
}