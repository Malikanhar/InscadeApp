package com.example.kilam.inscadeapp

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.kilam.inscadeapp.R.layout.activity_login
import com.example.kilam.inscadeapp.R.layout.dialog_forgetpass
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class ActivityLogin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_login)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        val mAuth = FirebaseAuth.getInstance()
        if(mAuth.currentUser != null){
            startActivity(Intent(this, ActivityMain::class.java))
            finish()
        }

        btn_daftar_baru.setOnClickListener { startActivity(Intent(this, ActivityRegister::class.java))}

        btn_lupa_password.setOnClickListener{showDialog() }

        btn_login.setOnClickListener{
            var email = et_email.text.toString().trim()
            var pass = et_pass.text.toString().trim()

            if(validateForm(email, pass)){
                val progressDialog = ProgressDialog(this)
                progressDialog.setCancelable(false)
                progressDialog.setMessage("Logging in")
                progressDialog.show()
                mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this){task ->
                    if(task.isSuccessful){
                        val user = mAuth.currentUser
                        val intent = Intent(this, ActivityMain::class.java)
                        progressDialog.dismiss()
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this, "Email or password is incorrect", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }
            }
        }

    }
    private fun isEmailValid(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        val matcher = pattern.matcher(email)
        return matcher.matches() && email.isNotEmpty()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8
    }

    private fun validateForm(email: String, pass: String): Boolean{
        if(!isEmailValid(email)){
            Toast.makeText(this, "Please input a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        if(!isPasswordValid(pass)){
            Toast.makeText(this, "Please input a valid password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showDialog() {
        val mAuth = FirebaseAuth.getInstance()
        var dialogs = Dialog(this)

        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setContentView(dialog_forgetpass)

        dialogs.window.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT)

        dialogs.show()
        val etemail = dialogs.findViewById(R.id.et_forgot_password) as EditText
        val btnReset = dialogs.findViewById(R.id.btn_resetPass) as Button
        btnReset.setOnClickListener {
            etemail.isEnabled = false
            var email = etemail.text.toString().trim()
            if(!isEmailValid(email)){
                Toast.makeText(this,"Please input a valid email", Toast.LENGTH_SHORT).show()
            }
            else{
                val progressDialog = ProgressDialog(this)
                progressDialog.setCancelable(false)
                progressDialog.setMessage("Sending email verification")
                progressDialog.show()

                mAuth!!.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email verification sent", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                            etemail.setText("")
                        } else {
                            Toast.makeText(this, "Failed to sent email verifivarion",
                                Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                    }
                dialogs.dismiss()
            }
            etemail.isEnabled = true
        }
    }
}