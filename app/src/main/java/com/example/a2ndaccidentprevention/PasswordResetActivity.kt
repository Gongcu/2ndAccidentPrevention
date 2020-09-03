package com.example.a2ndaccidentprevention

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.emailEditText
import kotlinx.android.synthetic.main.activity_password_reset.*


class PasswordResetActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)

        auth = FirebaseAuth.getInstance()

        sendBtn.setOnClickListener {
            resetMail()
        }
        backBtn.setOnClickListener {
            finish()
        }
    }
    private fun resetMail() {
        var email = emailEditText.text.toString()
        if (email.isNotEmpty()) {
            auth.sendPasswordResetEmail(email).addOnCompleteListener(this){task ->
                    if(task.isSuccessful){
                        Toast.makeText(this, "비밀번호 재설정 메일을 보냈습니다.",Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(this, "비밀번호 재설정 메일을 보내기에 실패했습니다.", Toast.LENGTH_SHORT) .show()

                    }
                }
        } else {
            Toast.makeText(this, "비밀번호 재설정 메일을 보내기에 실패했습니다.", Toast.LENGTH_SHORT) .show()
        }
    }
}
