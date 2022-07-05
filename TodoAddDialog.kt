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

//할일 추가시 밑에서 올라오는 다이얼로그
class TodoAddDialog(val context: Context, var date: Date) {
    private val dialog = BottomSheetDialog(context)
    lateinit var binding: TodoDialogBinding

    //사용자 다이얼로그 창 띄우기
    @SuppressLint("SetTextI18n")
    fun showDialog() {
        //todo_dialog 화면 가져오기
        binding = TodoDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        val month = SimpleDateFormat("MM", Locale.getDefault()).format(date.time)
        val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault()).format(date.time)
        val dayName: String = SimpleDateFormat("E", Locale.KOREA).format(date.time)

        binding.tvDate.text = "${month}월 ${dayOfMonth}일 ${dayName}"
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
                    //월이 0부터 시작하여 1을 더해주어야함
                    month = monthOfYear + 1
                    //선택한 날짜의 요일을 구하기 위한 calendar
                    val calendar = Calendar.getInstance()
                    //선택한 날짜 세팅
                    calendar.set(year, monthOfYear, dayOfMonth)
                    date = calendar.time
                    val simpleDateFormat = SimpleDateFormat("E", Locale.KOREA)
                    val dayName: String = simpleDateFormat.format(date)

                    binding.tvDate.text = "${month}월 ${dayOfMonth}일 $dayName"
                },
                year,
                month,
                day
            )

            dateEvent.show()
        }

        //할일 추가 이벤트 등록: 파이어베이스 연동
        binding.addTodo.setOnClickListener {
            if(binding.edtTodoTitle.text.isNotEmpty()) {
                //파이어스토어에서 UID 정보 받기
                val userId = Firebase.firebaseAuth.currentUser?.uid

                //데이터 클래스 생성
                val todoData = TodoData(
                    userId!!,
                    binding.edtTodoTitle.text.toString(),
                    SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(date),
                    false,
                0)

                //할일 추가
                val firebase = Firebase()
                firebase.addTodo(context, todoData)

                val todoFragment = (context as MainActivity).pagerAdapter.createFragment(1)
                val addDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.time)
                val addDateFormatter = LocalDate.parse(addDate)
                val bundle = Bundle()

                bundle.putInt("year", addDateFormatter?.year!!)
                bundle.putInt("month", addDateFormatter.month.value)
                bundle.putInt("day", addDateFormatter.dayOfMonth)
                todoFragment.arguments = bundle
                todoFragment.onStart()

                //다이얼로그 창 닫기
                dialog.dismiss()
            }else{
                //할일 제목 재요청
                Toast.makeText(context, "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        //창 끄기 이벤트 등록
        binding.ivClose.setOnClickListener {
            dialog.dismiss()
        }
    }
}
