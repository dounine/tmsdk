package com.dounine.tmsdk.util

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.DeviceUtils
import java.util.*

class DeviceUtil {
    companion object {
        private const val DEVICE_ID = "DEVICE_ID"
        fun getDeviceId(context: Context): String? {
            val spHelper =
                SPHelper.getInstance(context)
            var deviceId: String? = spHelper.getString(DEVICE_ID)
            Log.i("TMSdk", deviceId.toString())
            if (TextUtils.isEmpty(deviceId)) {
                deviceId = UUID.randomUUID().toString().replace("-", "")
                spHelper.putString(DEVICE_ID, deviceId)
            }
            return deviceId
        }

        fun getModel(): String {
            return DeviceUtils.getModel()
        }

        fun getBrand(): String {
            return DeviceUtils.getManufacturer()
        }
    }
}