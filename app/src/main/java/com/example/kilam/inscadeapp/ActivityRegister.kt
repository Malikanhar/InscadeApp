package com.example.kilam.inscadeapp

import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import com.example.kilam.inscadeapp.R.layout.activity_register
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class ActivityRegister:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_register)

        val mAuth = FirebaseAuth.getInstance()
        btn_daftar.setOnClickListener {
            var name = tiet_nama.text.toString().trim()
            var notelp = tiet_notelp.text.toString().trim()
            var email = tiet_email.text.toString().trim()
            var pass = tiet_password.text.toString().trim()

            if (validateForm(name, notelp, email, pass)) {
                val progressDialog = ProgressDialog(this)
                progressDialog.setCancelable(false)
                progressDialog.setMessage("Registering your account")
                progressDialog.show()

                mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Your account has created", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                            finish()
                        } else {
                            Toast.makeText(this, "There's an empty field", Toast.LENGTH_SHORT).show()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun validateForm(name: String, notelp: String, email: String, password: String): Boolean {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(notelp) || !isEmailValid(email) || !isPasswordValid(password)) {
            Toast.makeText(this, "There's an empty field", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun isEmailValid(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        val matcher = pattern.matcher(email)
        return matcher.matches() && email.isNotEmpty()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
}