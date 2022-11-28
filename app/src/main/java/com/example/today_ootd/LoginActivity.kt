package com.example.today_ootd


import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.today_ootd.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    var auth : FirebaseAuth? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

//        로그인 되어있는지 확인
        val currentUser = auth!!.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 회원가입 버튼
        val signup = binding.signupButton
        signup.setOnClickListener {
            signUp()
        }

        // 로그인 버튼
        val loginButton = binding.signInButton
        loginButton.setOnClickListener {
            signIn()
        }


        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 구글 로그인 버튼
        val googleLoginButton = binding.googleBtn
        googleLoginButton.setOnClickListener{
            googleLogin()
        }
    }

    fun googleLogin() {
        var i = googleSignInClient.signInIntent
        googleLoginResult.launch(i)
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

                        // Get permission
                        val permissionList = arrayOf<String>(
                            // 위치 권한
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        )

                        // 권한 요청
                        ActivityCompat.requestPermissions(this@LoginActivity, permissionList, 1)
                    }
                }else{
                    Toast.makeText(this, "등록되지 않은 이메일 혹은 잘못된 비밀번호입니다.", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun firebaseAuthWithGoogle(idToken: String?) {
        var credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful){
                if (auth?.currentUser!!.isEmailVerified){
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                else {

                }
            }
            else {

            }
        }
    }

    var googleLoginResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == RESULT_OK){
            var task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException){

            }

        }
        else {
            println("$RESULT_CANCELED bye")
        }
    }
}