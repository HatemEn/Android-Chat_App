package com.me.hatem.a02_kt_fa.Controller

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.widget.Toast
import com.me.hatem.a02_kt_fa.R
import com.me.hatem.a02_kt_fa.Services.AuthService
import com.me.hatem.a02_kt_fa.Services.UserDataService
import com.me.hatem.a02_kt_fa.Utilities.BROADCAST_USER_DATA_CHANGE
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*

class SignUpActivity : AppCompatActivity() {

    var avatarName  = "profileDefault"
    var avatarColor = "[0.5,0.5,0.5,1]"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        progressBarController(false)
    }

    fun generateUserAvatar(view: View) {
        val random  = Random()
        val color   = random.nextInt(2)
        val avatar  = random.nextInt(28)
        if (color == 1) {
            avatarName = "light$avatar"
        } else {
            avatarName = "dark$avatar"
        }
        val resourceID = resources.getIdentifier(avatarName,"drawable",packageName)
        createUserAvatarImage.setImageResource(resourceID)
    }

    fun generateBackgroundColor(view: View) {
        val random  = Random()
        val r       = random.nextInt(255)
        val g       = random.nextInt(255)
        val b       = random.nextInt(255)
        createUserAvatarImage.setBackgroundColor(Color.rgb(r,g,b))

        val savedR = r.toDouble() / 255
        val savedG = g.toDouble() / 255
        val savedB = b.toDouble() / 255
        avatarColor = "[$savedR,$savedG,$savedB,1]"
    }

    fun createAccount(view: View) {
        val name        = createUserNameText.text.toString()
        val email       = createUserEmailText.text.toString()
        val password    = createUserPasswordText.text.toString()
        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            progressBarController(true)
            AuthService.registerUser(email, password) {   registerSuccess ->
                if (registerSuccess) {
                    AuthService.loginUser(email, password) {  loginSuccess ->
                        if (loginSuccess) {
                            AuthService.createUser(name, email, avatarName, avatarColor) { createSuccess ->
                                if (createSuccess) {
                                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                                    LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChange)
                                    progressBarController(false)
                                    finish()
                                } else errorToasting()
                            }
                        } else errorToasting()
                    }
                } else errorToasting()
            }
        } else errorToasting("Please fill the name & email and the password... ")
    }

    fun errorToasting(message: String = "Something went wrong!. Please try again...") {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        progressBarController(false)
    }

    fun progressBarController(enable: Boolean) {
        if (enable) createProgressBar.visibility = View.VISIBLE
        else createProgressBar.visibility = View.INVISIBLE

        createAccount.isEnabled = !enable
        createBackgroundColor.isEnabled = !enable
        createUserAvatarImage.isEnabled = !enable
    }

}