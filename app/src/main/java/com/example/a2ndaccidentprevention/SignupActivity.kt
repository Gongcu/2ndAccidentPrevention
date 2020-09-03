package com.example.a2ndaccidentprevention

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_signup.*


class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        signUpBtn.setOnClickListener {
            signUp()
        }
        gotoLoginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    private fun signUp() {
        var email = emailEditText.text.toString()
        var password =pwEditText.text.toString()
        var passwordCheck = pwCheckEditText.text.toString()


        if (email.isNotEmpty()&& password.isNotEmpty() && passwordCheck.isNotEmpty()) {
            if (password == passwordCheck) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(applicationContext, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText( applicationContext,"회원가입에 실패했습니다.",Toast.LENGTH_SHORT).show()
                            task.exception?.printStackTrace()
                        }
                    }
            } else {
                Toast.makeText(this, "비밀번호기 서로 다릅니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        } else Toast.makeText(this, "값을 입력해주세요.", Toast.LENGTH_SHORT) .show()
    }
}
