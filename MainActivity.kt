package com.example.cally

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.cally.databinding.ActivityMainBinding
import com.example.cally.databinding.TabLayoutBinding
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var pagerAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Fragment PagerAdapter 생성
        pagerAdapter = PagerAdapter(this)

        //fragment 객체화하여 어뎁터에 저장
        //일정
        pagerAdapter.addFragment(CalendarFragment())
        //할일
        pagerAdapter.addFragment(TodoFragment())
        //친구목록
        pagerAdapter.addFragment(FriendsFragment())
        //로그아웃, 설정
        pagerAdapter.addFragment(Setting())

        //viewPager와 어뎁터 연결
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.isUserInputEnabled = false

        //tabLayout과 viewPager 연결
        TabLayoutMediator(binding.tabLayout, binding.viewPager){ tab, position ->
            //tab position 기준으로 뷰 생성
            tab.setCustomView(createTabView(position))
        }.attach()
    }

    //tab view 생성
    private fun createTabView(position: Int): View {
        val tabBinding = TabLayoutBinding.inflate(layoutInflater)
        when (position) {
            0 -> {
                tabBinding.ivTabIcon.setImageResource(R.drawable.calendar)
                tabBinding.tvTabTitle.text = "캘린더"
            }
            1 -> {
                tabBinding.ivTabIcon.setImageResource(R.drawable.todo)
                tabBinding.tvTabTitle.text = "할일"
            }
            2 -> {
                tabBinding.ivTabIcon.setImageResource(R.drawable.friends_list)
                tabBinding.tvTabTitle.text = "친구목록"
            }
            3 -> {
                tabBinding.ivTabIcon.setImageResource(R.drawable.setting)
                tabBinding.tvTabTitle.text = "설정"
            }
        }
        return tabBinding.root
    }
}