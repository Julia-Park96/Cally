package com.example.cally

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

//설정 프래그먼트: 폰트 사이즈, 테마, 친구 추가, 알림설정, 로그아웃
class SettingFragment: AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FragmentManager를 얻어온 후 transaction 시작
        // contatiner에 Fragment 변경하고 commit을 호출하여 transaction 적용
        supportFragmentManager.beginTransaction()
            .replace(R.id.settingLayout, Setting(), "fragment_setting")
            .commit()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference)
            : Boolean {
        // Instantiate the new Fragment
        val args = pref.extras
        val fragment = pref.fragment?.let {
            supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                it
            )
        }
        fragment?.arguments = args
        fragment?.setTargetFragment(caller, 0)
        // Replace the existing Fragment with the new Fragment
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settingLayout, fragment)
                .addToBackStack(null)
                .commit()
        }
        return true
    }
}