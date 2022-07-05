package com.example.cally

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

//MainActivity의 ViewPager에 Fragment를 연결하는 어뎁터
class PagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    //멤버변수
    private val fragmentArrayList = ArrayList<Fragment>()

    //fragmentArrayList의 개수
    override fun getItemCount(): Int = fragmentArrayList.size

    //fragment 전달
    override fun createFragment(position: Int): Fragment = fragmentArrayList[position]

    //멤버함수: fragment 추가 기능
    fun addFragment(fragment: Fragment){
        fragmentArrayList.add(fragment)
    }
}
