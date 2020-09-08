package com.example.a2ndaccidentprevention.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.a2ndaccidentprevention.R
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private val TAG: String = "LoginActivity"
    private val GOOGLE_LOGIN_CODE = 1001
    private lateinit var auth: FirebaseAuth //늦은 초기화이며 타입은 FirebaseAuth 변수명은 auth
    private lateinit var uid: String
    private lateinit var token: String
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var gso: GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        callbackManager = CallbackManager.Factory.create()
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        gotoSignupBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        loginBtn.setOnClickListener {
            firebaseLogin()
        }
        facebook_sign_in_btn.setOnClickListener {
            //현재 미구현 카카오 로그인으로 대체 생각 중
        }
        google_sign_in_btn.setOnClickListener {
            googleLogin()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data) //facebook
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_LOGIN_CODE) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account: GoogleSignInAccount ?= task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // ...
            }
        }
    }
    private fun firebaseLogin(){
        var email = emailEditText.text.toString()
        var password = pwEditText.text.toString()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        uid=task.result!!.user!!.uid // !!: 강제로 not null처리
                        registerPushToken()
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "로그인에 실패했습니다.",Toast.LENGTH_SHORT).show()
                        // ...
                    }
                }

        } else {
            Toast.makeText(this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }

    }
    private fun googleLogin() {
        val intent = googleSignInClient.signInIntent
        startActivityForResult(intent, GOOGLE_LOGIN_CODE)
    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")
                        uid = auth.currentUser!!.uid
                        registerPushToken()
                    } else {
                        Log.w(TAG,"signInWithCredential:failure",task.exception)
                        Toast.makeText(
                            this@LoginActivity,"인증에 실패했습니다.",Toast.LENGTH_SHORT
                        ).show()
                    }
                })
    }

    private fun registerPushToken(){//firestore에 토큰 값을 저장하고 성공한다면 mainactivity로 이동
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                token = task.result!!.token
                val map: MutableMap<String, Any> = HashMap()
                map["token"] = token
                FirebaseFirestore.getInstance().collection("tokens").document(uid).set(map)//firestore에 개인의 고유 uid값 문서에 token 값을 저장
                var intent = Intent(applicationContext, MainActivity::class.java)
                intent.putExtra("uid", uid)
                intent.putExtra("token", token)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
            })
    }
}