package com.dounine.tmsdk.util

import android.content.Context
import android.content.SharedPreferences

class SPHelper {
    companion object {
        private var spHelper: SPHelper? = null

        class SPHelper(val sSettings: SharedPreferences) {
            @Synchronized
            fun getString(key: String): String? {
                return sSettings.getString(key, "")
            }

            @Synchronized
            fun putString(key: String, value: String) {
                val editor = sSettings.edit()
                editor.putString(key, value)
                editor.commit()
            }
        }

        @Synchronized
        fun getInstance(context: Context): SPHelper {
            if (spHelper == null) {
                val sSettings =
                    context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
                spHelper = SPHelper(sSettings)
            }
            return spHelper as SPHelper
        }
    }
}