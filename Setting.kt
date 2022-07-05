package com.example.cally

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.cally.Firebase.Companion.userData
import com.example.cally.databinding.FragmentSettingBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


open class Setting : PreferenceFragmentCompat() {
    lateinit var binding: FragmentSettingBinding

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.setting, rootKey)
        Log.d("Park", "설정창구현")

        //-------------------로그아웃------------------//
        var logout: Preference? = findPreference("logout")
        var googleSignInClient :  GoogleSignInClient

        logout?.setOnPreferenceClickListener { preference ->
            Log.d("Park", "로그아웃버튼클릭확인")

            FirebaseAuth.getInstance().signOut()
            FirebaseAuth.getInstance().currentUser?.delete()

            Firebase.firebaseAuth.signOut()
            Firebase.firebaseAuth.currentUser?.delete()

            var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = view?.let { GoogleSignIn.getClient(it.context, gso) }!!
            googleSignInClient?.signOut()?.addOnCompleteListener{
                activity?.finish()
                startActivity(Intent(activity, SplashActivity::class.java))
                Log.d("Hwang", "로그아웃 확인중")
            }

            Log.d("Park", "로그아웃 확인")
            true
        } //end of logout

        //-------------------친구추가------------------//
        val findFriend: EditTextPreference? = findPreference("find_friend")
        findFriend?.isVisible = true

        //EditText 초기화
        findFriend?.setOnPreferenceClickListener {
            findFriend.text = ""
            true
        }

        //추가 버튼 클릭시 유저 검색, 친구 추가
        findFriend?.setOnPreferenceChangeListener { preference, newValue ->
            if (newValue != null) {
                getFriendEmail(newValue.toString(), preference.context)
            }
            true
        }
    }

    //유저정보 가져오기
    private fun getFriendEmail(friendEmail: String, context: Context){
        addFriend(friendEmail, context)
        Log.d("Hwang", "userData: $userData")
}

    //데이터베이스에 FriendData 저장
    private fun addFriend(email: String, context: Context) {
        val firebase = Firebase()
        val currentUser = Firebase.firebaseAuth.currentUser
        Firebase.db.collection("User")
            .whereEqualTo("userEmail", email)
            .get()
            .addOnSuccessListener { result ->
                if(result.documents.isNotEmpty()){
                    userData = result.documents[0].toObject(UserData::class.java)!!
                    Log.d("Hwang", "사용자 정보 가져오기 성공, ${userData}")

                    firebase.addGroup(currentUser!!.uid, userData, context)
                }else{
                    Toast.makeText(context, "사용자를 찾을 수 없습니다. " +
                            "\n이메일을 다시 확인해주세요", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Log.d("Hwang", "사용자 정보 가져오기 실패, $it")
            }
    }
}