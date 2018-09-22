package com.me.hatem.a02_kt_fa.Controller

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.me.hatem.a02_kt_fa.R
import com.me.hatem.a02_kt_fa.Services.AuthService
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressBarController(false)
    }

    fun loginClicked(view: View) {
        val email       = loginUserEmailText.text.toString()
        val password    = loginPasswordText.text.toString()
        hideKeyboard()
        progressBarController(true)
        if (email.isNotEmpty() && password.isNotEmpty()) {
            AuthService.loginUser(email, password) {  loginSuccess ->
                if (loginSuccess) {
                    AuthService.findUserByEmail(this) { findSuccess ->
                        if (findSuccess) {
                            progressBarController(false)
                            finish()
                        } else errorToasting()
                    }
                } else errorToasting()
            }
        } else errorToasting("Please fill the name & email and the password... ")
    }

    fun signUpClicked(view: View) {
        val toSignUp = Intent(this, SignUpActivity::class.java)
        startActivity(toSignUp)
        finish()
    }

    fun errorToasting(message: String = "Something went wrong!. Please try again...") {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        progressBarController(false)
    }

    fun progressBarController(enable: Boolean) {
        if (enable) loginProgressBar.visibility = View.VISIBLE
        else loginProgressBar.visibility = View.INVISIBLE

        loginBtn.isEnabled = !enable
        signupBtn.isEnabled = !enable
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (inputManager.isAcceptingText)
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}
