package com.example.a2ndaccidentprevention.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.example.a2ndaccidentprevention.R
import com.example.a2ndaccidentprevention.ViewModel
import com.example.a2ndaccidentprevention.room.Alert
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SplashActivity : AppCompatActivity() {
    val viewModel:ViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed(SplashHandler(this), 200)
    }

    inner class SplashHandler(var context: Context) : Runnable {
        private val user = Firebase.auth.currentUser

        override fun run() {
            if(viewModel.get().value ==null){
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    viewModel.insert(Alert(1,sound=true,vibration = true))
                }
            }

            if(user==null){//현재 사용자의 정보가 없다면 LoginActivity로 이동
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            }else{
                FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }//사용자의 정보가 있을 경우
                        val intent = Intent(context, MainActivity::class.java)
                        val token = task.result!!.token
                        val map: MutableMap<String, Any> = HashMap()
                        map["token"] = token
                        FirebaseFirestore.getInstance().collection("tokens").document(user.uid).set(map)
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
