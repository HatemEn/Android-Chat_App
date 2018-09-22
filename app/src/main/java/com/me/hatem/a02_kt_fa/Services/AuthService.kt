package com.me.hatem.a02_kt_fa.Services

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.me.hatem.a02_kt_fa.Controller.App
import com.me.hatem.a02_kt_fa.Utilities.*
import org.json.JSONException
import org.json.JSONObject

object AuthService {

    fun registerUser(email: String, password: String, complete: (Boolean)-> Unit) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)
        val requestBody = jsonObject.toString()

        val registerRequest = object : StringRequest(Method.POST, URL_REGISTER, Response.Listener { response ->
            complete(true)
        }, Response.ErrorListener { error ->
            Log.d("ERROR","Could not register user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        App.prefs.requestQueue.add(registerRequest)
    }

    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit) {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("password", password)
        val requestBody = jsonObject.toString()

        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener { response ->
            try {
                App.prefs.email       = response.getString("user")
                App.prefs.authToken   = response.getString("token")
                App.prefs.isLoggedIn  = true
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON","EXC: " + e.localizedMessage)
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR","Could not login user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        App.prefs.requestQueue.add(loginRequest)
    }

    fun createUser(name: String, email: String, avatarName: String,
                   avatarColor: String, complete: (Boolean) -> Unit) {
        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("email", email)
        jsonObject.put("avatarName", avatarName)
        jsonObject.put("avatarColor", avatarColor)
        val requestBody = jsonObject.toString()

        val createUserRequest = object : JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener {  response ->
            try {
                UserDataService.id          = response.getString("_id")
                UserDataService.name        = response.getString("name")
                UserDataService.avatarName  = response.getString("avatarName")
                UserDataService.avatarColor = response.getString("avatarColor")
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON","EXC: " + e.localizedMessage)
                complete(false)
            }
        }, Response.ErrorListener { error ->
            Log.d("ERROR","Could not add user: $error")
            complete(false)
        }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val header = HashMap<String, String>()
                header.put("Authorization", "Bearer ${App.prefs.authToken}")
                return header
            }
        }

        App.prefs.requestQueue.add(createUserRequest)

    }

    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit) {
        val findRequest = object : JsonObjectRequest(Method.GET, "$URL_GET_USER${App.prefs.email}", null, Response.Listener {
            try {
                UserDataService.name        = it.getString("name")
                UserDataService.avatarName  = it.getString("avatarName")
                UserDataService.avatarColor = it.getString("avatarColor")
                UserDataService.id          = it.getString("_id")
                val userDataChange          = Intent(BROADCAST_USER_DATA_CHANGE)
                LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                complete(true)
            } catch (e: JSONException) {
                Log.d("JSON","EXC: " + e.localizedMessage)
                complete(false)
            }
        }, Response.ErrorListener {
            Log.d("ERROR","Could not add user: $it")
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
        App.prefs.requestQueue.add(findRequest)
    }
}