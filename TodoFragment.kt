package com.example.cally

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cally.databinding.FragmentTodoBinding
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

//할일 내용 넣는 프래그먼트
class TodoFragment : Fragment() {
    lateinit var binding: FragmentTodoBinding
    lateinit var todoWeeklyAdapter: TodoWeeklyAdapter
    lateinit var todoRecyclerAdapter: TodoRecyclerAdapter
    var days: ArrayList<LocalDate?>? = null
    val firebase = Firebase()
    var selectedDate: LocalDate = LocalDate.now()
    var calendar: Calendar = Calendar.getInstance()
    var todoDataList: MutableList<TodoData> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n", "ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodoBinding.inflate(inflater, container, false)
        setWeekView()

        //최초 날짜는 현재 일자로 지정
        binding.tvTodoDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(calendar.time)
        binding.tvTodoMonth.text = SimpleDateFormat("yyyy년 MM월", Locale.KOREA).format(calendar.time)

        //할일 목록 어뎁터 생성 및 연결
        todoRecyclerAdapter =TodoRecyclerAdapter(todoDataList)
        binding.recyclerView.adapter = todoRecyclerAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        // 리사이클러뷰에 스와이프, 드래그 기능 달기
        val todoSwipeHelperCallback = TodoSwipeHelperCallback(todoRecyclerAdapter).apply {
            // 스와이프한 뒤 고정시킬 위치 지정
            setClamp(resources.displayMetrics.widthPixels.toFloat()/3)    // 1080 / 3 = 360
        }
        ItemTouchHelper(todoSwipeHelperCallback).attachToRecyclerView(binding.recyclerView)

        // 다른 곳 터치 시 기존 선택했던 뷰 닫기
        binding.recyclerView.setOnTouchListener { _, _ ->
            todoSwipeHelperCallback.removePreviousClamp(binding.recyclerView)
            false
        }

        //월 클릭시 이벤트 등록: DatePickerDialog 띄우기
        binding.linearLayout.setOnClickListener {
            val dateEvent = context?.let { it1 ->
                DatePickerDialog(
                    it1,
                    { _, year, monthOfYear, dayOfMonth ->
                        //월이 0부터 시작하여 1을 더해주어야함
                        //선택한 날짜 세팅
                        calendar.set(year, monthOfYear, dayOfMonth)

                        //선택한 날짜로 일자 변경
                        binding.tvTodoDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(calendar.time)

                        //선택한 날짜로 상단 월 변경
                        binding.tvTodoMonth.text = SimpleDateFormat("yyyy년 MM월", Locale.KOREA).format(calendar.time)

                        //선택한 날짜로 스캐줄러 업데이트
                        selectedDate = LocalDate.of(year, monthOfYear+1, dayOfMonth)

                        days = daysInWeekArray(selectedDate)
                        todoWeeklyAdapter.daysOfMonth = days
                        todoWeeklyAdapter.notifyDataSetChanged()

                        //할일 목록 가져오기
                        notifyTodoAdapter(todoRecyclerAdapter)
                    },
                    selectedDate.year,
                    selectedDate.monthValue-1,
                    selectedDate.dayOfMonth
                )
            }

            //다이얼로그 띄우기
            if (dateEvent != null) {
                dateEvent.show()
            }
        }

        //FloatingActionButton 이벤트 등록: TodoDialog 화면 띄우기
        binding.floatingActionButton.setOnClickListener {
            val todoAddDialog = TodoAddDialog(requireContext(), calendar.time)
            todoAddDialog.showDialog()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if(arguments != null){
            val year = requireArguments().getInt("year")
            val month = requireArguments().getInt("month")
            val day = requireArguments().getInt("day")
            Log.d("Hwang", "todoFragment: $year $month $day")
            calendar.set(year, month-1, day)

            binding.tvTodoDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(calendar.time)
            binding.tvTodoMonth.text = SimpleDateFormat("yyyy년 MM월", Locale.KOREA).format(calendar.time)

            todoRecyclerAdapter.updatePosition()
            notifyTodoAdapter(todoRecyclerAdapter)

            try {
                arguments?.clear()
            }catch (e: Exception){
                Log.d("Hwang", "argument clear 오류")
            }
        }else{
            //할일 목록 가져오기
            binding.tvTodoDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(calendar.time)
            binding.tvTodoMonth.text = SimpleDateFormat("yyyy년 MM월", Locale.KOREA).format(calendar.time)
            todoRecyclerAdapter.updatePosition()
            notifyTodoAdapter(todoRecyclerAdapter)
        }
    }

    //화면 설정
    private fun setWeekView() {
        days = daysInWeekArray(selectedDate)
        todoWeeklyAdapter = TodoWeeklyAdapter(requireContext(), days)
        val layoutManager: RecyclerView.LayoutManager = GridLayoutManager(context, 7)
        binding.weeklyRecycler.layoutManager =layoutManager
        binding.weeklyRecycler.adapter = todoWeeklyAdapter
    }

    //할일 일자가 포함된 주의 날짜 가져오기
    private fun daysInWeekArray(selectedDate: LocalDate): ArrayList<LocalDate?>? {
        val days: ArrayList<LocalDate?> = ArrayList()
        var current: LocalDate? = sundayForDate(selectedDate)
        val endDate = current!!.plusWeeks(1)
        while (current!!.isBefore(endDate)) {
            days.add(current)
            current = current.plusDays(1)
        }
        return days
    }

    //주별 일요일 날짜 받기
    private fun sundayForDate(selectedDate: LocalDate): LocalDate? {
        var current = selectedDate
        val oneWeekAgo = current.minusWeeks(1)
        while (current.isAfter(oneWeekAgo)) {
            if (current.dayOfWeek == DayOfWeek.SUNDAY) return current
            current = current.minusDays(1)
        }
        return null
    }

    //todoRecyclerAdpater 업데이트
    private fun notifyTodoAdapter(todoRecyclerAdapter: TodoRecyclerAdapter){
        //일정 목록 가져오기
        firebase.selectTodoList(todoRecyclerAdapter, calendar.time)
    }
}