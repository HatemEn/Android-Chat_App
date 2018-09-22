package com.me.hatem.a02_kt_fa.Services

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.LocalBroadcastManager
import com.me.hatem.a02_kt_fa.Controller.App
import com.me.hatem.a02_kt_fa.Utilities.BROADCAST_USER_DATA_CHANGE
import java.util.*

object UserDataService {
    var id          = ""
    var name        = ""
    var avatarName  = ""
    var avatarColor = ""


    fun logout(context: Context) {
        id          = ""
        name        = ""
        avatarName  = ""
        avatarColor = ""
        App.prefs.isLoggedIn  = false
        App.prefs.authToken   = ""
        App.prefs.email       = ""
        MessageService.clearMessages()
        MessageService.clearChannels()
        val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
        LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
    }
    fun returnAvatarColor(components: String = avatarColor) : Int {
        val strippedColor = components
                .replace("[","")
                .replace("]", "")
                .replace(",", " ")
        var r = 0
        var g = 0
        var b = 0
        val scanner = Scanner(strippedColor)
        if (scanner.hasNext()) {
            r = (scanner.nextDouble() * 255).toInt()
            g = (scanner.nextDouble() * 255).toInt()
            b = (scanner.nextDouble() * 255).toInt()
        }
        return Color.rgb(r, g, b)
    }
}