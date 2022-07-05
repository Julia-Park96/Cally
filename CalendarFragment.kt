package com.example.cally

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cally.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.*

//일정 화면
class CalendarFragment : Fragment() {
    lateinit var binding: FragmentCalendarBinding
    lateinit var calendarRecyclerAdapter: CalendarRecyclerAdapter

    private val firebase = Firebase()
    private val calendar = Calendar.getInstance()
    var scheduleDataList: MutableList<ScheduleData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCalendarBinding.inflate(inflater, container, false)

        //최초 날짜는 당일로 지정
        binding.diaryTextView.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(calendar.time) + " 일정"

        //recycler adapter 등록
        calendarRecyclerAdapter = CalendarRecyclerAdapter(scheduleDataList, calendar)
        binding.calendarRecycler.adapter = calendarRecyclerAdapter
        binding.calendarRecycler.layoutManager = LinearLayoutManager(context)

        // 리사이클러뷰에 스와이프, 드래그 기능 달기
        val calendarSwipeHelperCallback = CalendarSwipeHelperCallback(calendarRecyclerAdapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat()/3)    // 1080 / 3 = 360
        }
        ItemTouchHelper(calendarSwipeHelperCallback).attachToRecyclerView(binding.calendarRecycler)

        // 다른 곳 터치 시 기존 선택했던 뷰 닫기
        binding.calendarRecycler.setOnTouchListener { _, _ ->
            calendarSwipeHelperCallback.removePreviousClamp(binding.calendarRecycler)
            false
        }

        //날짜변경 이벤트
        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            //선택된 날짜로 TextView 변경
            binding.diaryTextView.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(calendar.time) + " 일정"
            //일정 목록 가져오기
            notifyAdapter(calendarRecyclerAdapter)
        }

        //FloatingActionButton 클릭 이벤트: CalendarAddDialog 띄우기
        binding.fabCalendar.setOnClickListener {
            val calendarAddDialog = CalendarAddDialog(requireContext(), calendar)
            calendarAddDialog.showDialog()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        notifyAdapter(calendarRecyclerAdapter)
    }

    //calendarRecyclerAdapter의 scheduleDataList 업데이트
    private fun notifyAdapter(calendarRecyclerAdapter: CalendarRecyclerAdapter){
        //일정 목록 가져오기
        firebase.selectScheduleList(calendarRecyclerAdapter, calendar.time)
    }
}