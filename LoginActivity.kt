package com.example.cally

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cally.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

//최초 로그인 화면
class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    //파이어베이스, 구글 클라이언트 전역변수 설정
    //private lateinit var auth: FirebaseAuth
    var googleSignInClient: GoogleSignInClient? = null
    val RC_SIGN_IN = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //구글 로그인
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogleSignIn.setOnClickListener {
            var signInIntent = googleSignInClient?.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    //다음 페이지로 넘어가기
    fun move2NextPage() {
        var currentUser = Firebase.firebaseAuth.currentUser
        if (currentUser != null) {
            //로그인시 UserData 추가
            val firebase = Firebase()
            firebase.addUser()

            startActivity(Intent(this, MainActivity::class.java))
            this.finish()
        }
    }

    //신용 확인
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(acct?.idToken, null)
        Firebase.firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                move2NextPage()
            }
        }
    }

    //앱이 계속 실행될 경우 다음 페이지로 이동
    override fun onResume() {
        super.onResume()
        move2NextPage()
    }


    //익셉션 잡기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
            }
        }
    }
}