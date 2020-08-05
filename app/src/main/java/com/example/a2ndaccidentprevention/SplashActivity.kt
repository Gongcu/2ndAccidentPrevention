package com.example.a2ndaccidentprevention

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val handler = Handler()
        handler.postDelayed(SplashHandler(this), 0)
    }

    private class SplashHandler(var context: Context) : Runnable { //코틀린에서는 클래스에 ()를 붙여 생성자를 만든다.
        private lateinit var auth: FirebaseAuth //늦은 초기화이며 타입은 FirebaseAuth 변수명은 auth

        override fun run() {
            auth = Firebase.auth
            var user = auth.currentUser

            if(user==null){//현재 사용자의 정보가 없다면 LoginActivity로 이동
                val intent = Intent(context, LoginActivity::class.java) //코틀린에서는 생성자로 받은 값을 초기화 없이 바로 사용 가능
                //intent.putExtra("key", value)
                context.startActivity(intent)
            }else{
                FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }//사용자의 정보가 있을 경우
                        val intent =Intent(context, MainActivity::class.java)
                        val token = task.result!!.token
                        val map: MutableMap<String, Any> = HashMap()
                        map["token"] = token
                        val uid = user.uid
                        FirebaseFirestore.getInstance().collection("tokens").document(uid).set(map)//firestore에 개인의 고유 uid값 문서에 token 값을 저장
                        intent.putExtra("uid", uid)
                        intent.putExtra("token", token)
                        context.startActivity(intent)
                    }).addOnFailureListener {
                        val intent =Intent(context, LoginActivity::class.java)
                        FirebaseAuth.getInstance().signOut()
                        context.startActivity(intent)
                    }
            }
        }
    }

    override fun onBackPressed() {
        //스플래시 화면에서 뒤로가기 불가
    }
}
