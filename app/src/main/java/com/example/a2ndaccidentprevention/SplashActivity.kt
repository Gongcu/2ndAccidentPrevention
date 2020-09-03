package com.example.a2ndaccidentprevention

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
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

        Handler().postDelayed(SplashHandler(this), 0)
    }

    private class SplashHandler(var context: Context) : Runnable {
        private lateinit var auth: FirebaseAuth

        override fun run() {
            auth = Firebase.auth
            var user = auth.currentUser

            if(user==null){//현재 사용자의 정보가 없다면 LoginActivity로 이동
                val intent = Intent(context, LoginActivity::class.java) //코틀린에서는 생성자로 받은 값을 초기화 없이 바로 사용 가능
                context.startActivity(intent) //SplahshHandler가 아닌 Activity에서의 startActvity를 위한 context.startActivity
            }else{
                FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }//사용자의 정보가 있을 경우
                        var intent = Intent(context, MainActivity::class.java)
                        val token = task.result!!.token
                        val map: MutableMap<String, Any> = HashMap()
                        map["token"] = token
                        val uid = user.uid
                        Log.d("token", token);
                        Log.d("uid", uid);
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
