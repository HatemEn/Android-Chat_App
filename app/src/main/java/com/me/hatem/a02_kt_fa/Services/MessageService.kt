package com.me.hatem.a02_kt_fa.Services

import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.me.hatem.a02_kt_fa.Controller.App
import com.me.hatem.a02_kt_fa.Model.Channel
import com.me.hatem.a02_kt_fa.Model.Message
import com.me.hatem.a02_kt_fa.Utilities.URL_GET_CHANNELS
import com.me.hatem.a02_kt_fa.Utilities.URL_GET_MESSAGES
import org.json.JSONException

object MessageService {
    val channels = ArrayList<Channel>()
    val messages = ArrayList<Message>()

    fun getChannels(complete: (Boolean) -> Unit) {
        val channelsRequest = object : JsonArrayRequest(Method.GET, URL_GET_CHANNELS, null, Response.Listener { response ->
            clearChannels()
            try {
                for (x in 0 until response.length()) {
                    val channel     = response.getJSONObject(x)
                    val channelName = channel.getString("name")
                    val channelDesc = channel.getString("description")
                    val channelId   = channel.getString("_id")
                    val newChannel  = Channel(channelName, channelDesc, channelId)
                    channels.add(newChannel)
                    complete(true)
                }
            } catch (e: JSONException) {
                Log.d("JSON", "EXC: ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR", "Could not retrieve channels")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header.put("Authorization", "Bearer ${App.prefs.authToken}")
                return header
            }
        }
        App.prefs.requestQueue.add(channelsRequest)
    }

    fun getMessages(channelId: String, complete: (Boolean) -> Unit) {
        val url = "$URL_GET_MESSAGES$channelId"
        val messagesRequest = object : JsonArrayRequest(Method.GET, url, null, Response.Listener { response ->
            clearMessages()
            try {
                for (x in 0 until response.length()) {
                    val message     = response.getJSONObject(x)
                    val id          = message.getString("_id")
                    val msgBody     = message.getString("messageBody")
                    val channelId   = message.getString("channelId")
                    val userName    = message.getString("userName")
                    val avatar      = message.getString("userAvatar")
                    val avaarColor  = message.getString("userAvatarColor")
                    val timeStamp   = message.getString("timeStamp")
                    val newMessage  = Message(msgBody, userName, channelId, avatar, avaarColor, id, timeStamp)
                    messages.add(newMessage)
                }
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON", "EXC: ${e.localizedMessage}")
                complete(false)
            }
        }, Response.ErrorListener {
            Log.d("ERROR", "Could not retrieve messages")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header.put("Authorization", "Bearer ${App.prefs.authToken}")
                return header
            }
        }
        App.prefs.requestQueue.add(messagesRequest)
    }

    fun clearChannels() {
        channels.clear()
    }

    fun clearMessages() {
        messages.clear()
    }
}