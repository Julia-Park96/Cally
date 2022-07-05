package com.example.cally

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.cally.databinding.CalendarDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class CalendarEditDialog(val context: Context, val scheduleData: ScheduleData, val calendar: Calendar, val option: Int)
    : View.OnClickListener {
    companion object{
        private val EDIT_CALENDAR = 0
        private val SHOW_CALENDAR = 1
    }

    lateinit var binding: CalendarDialogBinding
    private val dialog = BottomSheetDialog(context)
    private val firebase = Firebase()
    private var repeatType: String = ""
    private var repeatCycle: Int = 0

    fun showDialog() {
        //calendar_dialog 화면 가져오기
        binding = CalendarDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        //화면 설정
        setView(option)

        if(binding.swRepeat.isChecked){
            //반복 설정: 라디오버튼 드러내기
            binding.rgRepeatCycle.visibility = View.VISIBLE
            binding.edtRepeatDays.visibility = View.VISIBLE
        }else{
            //반복 해제: 라디오버튼 숨기기
            binding.rgRepeatCycle.visibility = View.GONE
            binding.edtRepeatDays.visibility = View.GONE
        }

        dialog.show()

        //이벤트 등록
        binding.ivCalendarClose.setOnClickListener(this)
        binding.ivCalendarSave.setOnClickListener(this)
        binding.tvScheduleDate.setOnClickListener(this)
        binding.swRepeat.setOnClickListener(this)
        binding.share.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            //다이얼로그 종료
            R.id.ivCalendarClose -> {
                dialog.dismiss()
            }
            R.id.ivCalendarSave -> {
                when(option){
                    //일정 수정시: 일정 추가 및 Firebase 저장
                    EDIT_CALENDAR -> saveCalendar(scheduleData)
                    //일정 상세보기 선택시: 다이얼로그 종료
                    SHOW_CALENDAR -> dialog.dismiss()
                }
            }
            //일자 설정
            R.id.tvScheduleDate -> {
                //calendarView 값으로 달력 설정
                val datePickerCalendar = calendar
                var year = datePickerCalendar.get(Calendar.YEAR)
                var month = datePickerCalendar.get(Calendar.MONTH)
                var day = datePickerCalendar.get(Calendar.DAY_OF_MONTH)
                datePickerCalendar.set(year, month, day)

                val dateEvent = DatePickerDialog(
                    context,
                    { _, year, monthOfYear, dayOfMonth ->
                        //월이 0부터 시작하여 1을 더해주어야함
                        month = monthOfYear + 1
                        //선택한 날짜 세팅
                        datePickerCalendar.set(year, monthOfYear, dayOfMonth)
                        calendar.time = datePickerCalendar.time
                        binding.tvScheduleDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(calendar.time)
                    },
                    year,
                    month,
                    day
                )

                dateEvent.show()
            }
            //반복 스위치 클릭시
            R.id.swRepeat -> {
                if (binding.swRepeat.isChecked) {
                    //반복 설정: 라디오 버튼 보이기
                    binding.rgRepeatCycle.visibility = View.VISIBLE
                    binding.edtRepeatDays.visibility = View.VISIBLE
                } else {
                    //반복 해제: 라디오버튼 숨기기
                    binding.rgRepeatCycle.visibility = View.GONE
                    binding.edtRepeatDays.visibility = View.GONE
                }
            }
        }
    }

    //화면 설정
    private fun setView(option: Int){
        when(option){
            EDIT_CALENDAR -> {
                //일정 정보만 수정하도록 숨기기
                binding.tvDialogTitle.text = "일정 수정"
                //일정, 제목, 상세내역만 변경 가능하도록 설정
                binding.share.visibility = View.GONE
                binding.swRepeat.visibility = View.GONE
                binding.rgRepeatCycle.visibility = View.GONE
                binding.edtRepeatDays.visibility = View.GONE
                binding.tvStartDate.visibility = View.GONE
                binding.tvEndDate.visibility = View.GONE
                binding.textView.visibility = View.GONE
                binding.edtScheduleDetail.isEnabled = true
                binding.edtScheduleTitle.isEnabled = true
            }
            SHOW_CALENDAR -> {
                binding.tvDialogTitle.text = "일정 상세 보기"
                //일정 공유 제외하고 모두 보이기
                binding.share.visibility = View.INVISIBLE
                binding.swRepeat.visibility = View.VISIBLE
                binding.rgRepeatCycle.visibility = View.VISIBLE
                binding.edtRepeatDays.visibility = View.VISIBLE
                binding.tvStartDate.visibility = View.VISIBLE
                binding.tvEndDate.visibility = View.VISIBLE
                binding.textView.visibility = View.VISIBLE

                //수정 및 터치 막기
                binding.swRepeat.isEnabled = false
                binding.rgRepeatCycle.isEnabled = false
                binding.edtRepeatDays.isEnabled = false
                binding.edtScheduleDetail.isEnabled = false
                binding.edtScheduleTitle.isEnabled = false
                binding.tvStartDate.isEnabled = false
                binding.tvEndDate.isEnabled = false
                binding.tvScheduleDate.isEnabled = false
            }
        }

        //변경된 데이터로 화면 수정
        binding.tvStartDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(dateFormatter(scheduleData.startDate))
        binding.tvEndDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(dateFormatter(scheduleData.endDate))
        binding.tvScheduleDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(dateFormatter(scheduleData.scheduleDate))
        binding.edtScheduleTitle.setText(scheduleData.scheduleTitle)
        binding.edtScheduleDetail.setText(scheduleData.scheduleDetail)
        binding.swRepeat.isChecked = scheduleData.isRepeated

        //뷰 설정
        if (binding.swRepeat.isChecked) {
            //반복 설정: 라디오 버튼 보이기
            binding.rgRepeatCycle.visibility = View.VISIBLE
            binding.edtRepeatDays.visibility = View.VISIBLE
        } else {
            //반복 해제: 라디오버튼 숨기기
            binding.rgRepeatCycle.visibility = View.GONE
            binding.edtRepeatDays.visibility = View.GONE
        }

        //반복 정보 받기
        setRepeatInfo(scheduleData.repeatType, scheduleData.repeatCycle)
    }

    //날짜 형식 변경: 데이터베이스에서 받은 문자열 날짜를 Date 형식으로 변경
    private fun dateFormatter(date: String): Date {
        val token = date.split('-')
        val year = token[0].toInt()
        val month = token[1].toInt()
        val day = token[2].toInt()
        val convertedDate: Calendar = Calendar.getInstance()
        convertedDate.set(year, month-1 , day)

        return convertedDate.time
    }

    //일정 수정
    private fun saveCalendar(scheduleData: ScheduleData){
        if (binding.edtScheduleTitle.text.isNotEmpty() &&
            binding.tvStartDate.text.isNotEmpty() &&
            binding.tvEndDate.text.isNotEmpty()
        ) {
            //반복 정보 받기
            if(binding.swRepeat.isChecked){
                getRepeatInfo()
            }
            //입력된 내용으로 일정 데이터 업데이트
            scheduleData.scheduleTitle = binding.edtScheduleTitle.text.toString()
            scheduleData.scheduleDetail = binding.edtScheduleDetail.text.toString()
            scheduleData.startDate = SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(calendar.time)
            scheduleData.endDate = SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(calendar.time)
            scheduleData.scheduleDate = SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(calendar.time)

            //업데이트된 내용으로 데이터베이스 변경
            firebase.updateSchedule(scheduleData)

            //CalendarFragment recyclerview 업데이트
            val calendarFragment = (context as MainActivity).pagerAdapter.createFragment(0)
            calendarFragment.onStart()

            //다이얼로그 창 닫기
            dialog.dismiss()
        } else {
            Toast.makeText(context, "일정 이름과 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    //라디오버튼에 따라 반복 타입과 주기 설정
    private fun getRepeatInfo(){
        when(binding.rgRepeatCycle.checkedRadioButtonId){
            //매일 반복
            R.id.rbEveryDay -> {
                repeatType = "day"
                repeatCycle = 1
            }
            //매주 반복
            R.id.rbEveryWeek -> {
                repeatType = "day"
                repeatCycle = 7
            }
            //매달 반복
            R.id.rbEveryMonth -> {
                repeatType = "month"
                repeatCycle = 1
            }
            //직접 입력(일 기준)
            R.id.rbGetText -> {
                repeatType = "day"
                repeatCycle = binding.edtRepeatDays.text.toString().toInt()
            }
        }
    }

    //반복 타입과 주기에 따라 라디오 버튼 설정
    private fun setRepeatInfo(repeatType: String, repeatCycle: Int){
        when(repeatType){
            "day"->{
                //매일
                if(repeatCycle==1) binding.rbEveryDay.isChecked = true
                //매주
                else binding.rbEveryWeek.isChecked = true
            }
            "month"-> {
                //매달
                binding.rbEveryMonth.isChecked = true
            }
            "text"->{
                //직접입력
                binding.rbGetText.isChecked = true
                binding.edtRepeatDays.setText("$repeatCycle")
            }
        }
    }
}