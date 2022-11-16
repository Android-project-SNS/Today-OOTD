package com.example.today_ootd

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        // 회원가입 버튼
        val signup = findViewById<Button>(R.id.signupButton)
        signup.setOnClickListener {
            signUp()
        }

        // 로그인 버튼
        val loginButton = findViewById<Button>(R.id.signInButton)
        loginButton.setOnClickListener {
            signIn()
        }

    }
    fun signUp(){
        val signupIntent = Intent(this, SignupActivity::class.java)
        startActivity(signupIntent)
    }

    fun signIn(){
        val id = findViewById<EditText>(R.id.email).text.toString()
        val password = findViewById<EditText>(R.id.password).text.toString()

        auth?.signInWithEmailAndPassword(id, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    // 로그인
                    val user : FirebaseUser?= task.result.user
                    if(user != null) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }else{
                    Toast.makeText(this, "등록되지 않은 이메일 혹은 잘못된 비밀번호입니다.", Toast.LENGTH_LONG).show()
                }
            }
    }
}