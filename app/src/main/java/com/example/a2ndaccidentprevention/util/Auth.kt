package com.example.a2ndaccidentprevention.util

import com.google.firebase.auth.FirebaseAuth

class Auth{
    companion object {
        private val TAG = Auth::class.java.simpleName
        private lateinit var auth: FirebaseAuth
        fun getUid() : String{
            auth = FirebaseAuth.getInstance()
            return auth.currentUser?.uid.toString()
        }
    }
}