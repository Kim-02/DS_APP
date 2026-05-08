package com.example.ds_safer.ui.screens.floormap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

fun decodeBase64ToBitmap(base64: String): Bitmap? {
    return try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}