package com.example.cally

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import com.example.cally.databinding.TodoDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

//할일 데이터 수정
class TodoEditDialog(val context: Context, var todoData: TodoData, val todoRecyclerAdapter: TodoRecyclerAdapter) {
    private val dialog = BottomSheetDialog(context)
    lateinit var binding: TodoDialogBinding
    val firebase = Firebase()

    @SuppressLint("SimpleDateFormat", "SetTextI18n", "NotifyDataSetChanged")
    fun showDialog(){
        //todo_dialog 화면 가져오기
        binding = TodoDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        //문자열 날짜를 필요한 날짜 서식으로 변경
        val formatter =  SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA)
        var date: Date = formatter.parse(todoData.todoDate)!!

        //현재 todoData로 화면 설정
        binding.edtTodoTitle.setText(todoData.todoTitle)
        binding.tvDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(date.time)
        binding.ivAdd.setImageResource(R.drawable.edit)
        binding.tvAdd.text = "할일 수정"

        dialog.show()

        //날짜 클릭시 DatePicker Dialog
        binding.tvDate.setOnClickListener {
            val datePickerCalendar = Calendar.getInstance()
            val year = datePickerCalendar.get(Calendar.YEAR)
            var month = datePickerCalendar.get(Calendar.MONTH)
            val day = datePickerCalendar.get(Calendar.DAY_OF_MONTH)

            val dateEvent = DatePickerDialog(
                context,
                { _, year, monthOfYear, dayOfMonth ->
                    month = monthOfYear + 1
                    //선택한 날짜의 요일을 구하기 위한 calendar
                    val calendar = Calendar.getInstance()
                    //선택한 날짜 세팅
                    calendar.set(year, monthOfYear, dayOfMonth)
                    date = calendar.time
                    binding.tvDate.text = "${SimpleDateFormat("MM", Locale.getDefault()).format(date.time)}월 " +
                            "${SimpleDateFormat("dd", Locale.getDefault()).format(date.time)}일 " +
                            SimpleDateFormat("E", Locale.KOREA).format(date.time)
                },
                year,
                month,
                day
            )

            dateEvent.show()
        }

        //할일 수정 이벤트 등록: 파이어베이스 연동
        binding.addTodo.setOnClickListener {
            if(binding.edtTodoTitle.text.isNotEmpty()) {
                //데이터 클래스 값 수정
                todoData.todoTitle = binding.edtTodoTitle.text.toString()
                todoData.todoDate = SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(date)

                //할일 변경
                firebase.updateTodo(todoData)

                val todoFragment = (context as MainActivity).pagerAdapter.createFragment(1)
                val editDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
                val editDateFormatter = LocalDate.parse(editDate)
                val bundle = Bundle()

                bundle.putInt("year", editDateFormatter?.year!!)
                bundle.putInt("month", editDateFormatter.month.value)
                bundle.putInt("day", editDateFormatter.dayOfMonth)
                todoFragment.arguments = bundle
                todoFragment.onStart()

                //다이얼로그 창 닫기
                dialog.dismiss()
            }else{
                //할일 제목 재요청
                Toast.makeText(context, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        //버튼 이벤트 설정 : 창 종료
        binding.ivClose.setOnClickListener {
            dialog.dismiss()
        }

    }
}