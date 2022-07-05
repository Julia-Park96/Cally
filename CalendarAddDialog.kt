package com.example.cally

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.cally.databinding.CalendarDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class CalendarAddDialog(val context: Context, val calendar: Calendar): View.OnClickListener {
    lateinit var binding: CalendarDialogBinding

    private val dialog = BottomSheetDialog(context)
    private val firebase = Firebase()
    private var repeatType: String = ""
    private var repeatCycle: Int = 0
    private var startDate: Date = calendar.time
    private var endDate: Date = calendar.time

    @SuppressLint("SetTextI18n")
    fun showDialog() {
        //calendar_dialog 화면 가져오기
        binding = CalendarDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        //일정 시작일과 종료일을 calendarView의 날짜로 지정하여 다이얼로그 띄우기
        binding.tvScheduleDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(calendar.time)
        binding.tvStartDate.text = binding.tvScheduleDate.text
        binding.tvEndDate.text = binding.tvScheduleDate.text

        binding.tvDialogTitle.text = "일정 추가"

        if(binding.swRepeat.isChecked){
            //반복 설정
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
        binding.tvStartDate.setOnClickListener(this)
        binding.tvEndDate.setOnClickListener(this)
        binding.swRepeat.setOnClickListener(this)
        binding.share.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            //다이얼로그 종료
            R.id.ivCalendarClose -> {
                dialog.dismiss()
            }
            //일정 추가 및 Firebase 저장
            R.id.ivCalendarSave -> {
                //현재 사용자 UID 가져오기
                val currentUser = Firebase.firebaseAuth.currentUser?.uid
                if (currentUser != null) {
                    saveCalendar(currentUser)
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
            //일정 시작일 지정
            R.id.tvStartDate -> {
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
                        startDate = datePickerCalendar.time
                        binding.tvStartDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(startDate)
                    },
                    year,
                    month,
                    day
                )

                dateEvent.datePicker.maxDate = calendar.time.time
                dateEvent.show()
            }
            //일정 종료일 지정
            R.id.tvEndDate -> {
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
                        endDate = datePickerCalendar.time
                        binding.tvEndDate.text = SimpleDateFormat("MM월 dd일 E", Locale.KOREA).format(endDate)
                    },
                    year,
                    month,
                    day
                )

                //최소 날짜를 시작일자 이후로 지정
                dateEvent.datePicker.minDate = calendar.time.time
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
            //친구에게 일정 공유
            R.id.share -> {
                if (binding.edtScheduleTitle.text.isNotEmpty() &&
                    binding.tvStartDate.text.isNotEmpty() &&
                    binding.tvEndDate.text.isNotEmpty()
                ) {
                    //친구 목록 띄우기: 친구 다이얼로그 화면
                    //반복 정보 받기
                    if(binding.swRepeat.isChecked){
                        getRepeatInfo()
                    }

                    //일정 데이터 생성
                    val scheduleData = ScheduleData(
                        Firebase.firebaseAuth.currentUser!!.uid,
                        binding.edtScheduleTitle.text.toString(),
                        SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(startDate),
                        SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(endDate),
                        SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(calendar.time),
                        binding.swRepeat.isChecked,
                        repeatType,
                        repeatCycle,
                        binding.edtScheduleDetail.text.toString()
                    )

                    //공유된 친구 목록 받아 일정 생성
                    val friendDialog = FriendDialog(context, scheduleData)
                    friendDialog.showDialog()

                    //다이얼로그 종료
                    dialog.dismiss()
                } else{
                    Toast.makeText(context, "일정 이름과 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //일정 추가
    private fun saveCalendar(userId: String){
        if (binding.edtScheduleTitle.text.isNotEmpty() &&
            binding.tvStartDate.text.isNotEmpty() &&
            binding.tvEndDate.text.isNotEmpty()
        ) {
            //반복 정보 받기
            if(binding.swRepeat.isChecked){
                getRepeatInfo()
            }

            //일정 데이터 생성
            val scheduleData = ScheduleData(
                userId,
                binding.edtScheduleTitle.text.toString(),
                SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(startDate),
                SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(endDate),
                SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(calendar.time),
                binding.swRepeat.isChecked,
                repeatType,
                repeatCycle,
                binding.edtScheduleDetail.text.toString()
            )

            //시작일자, 종료일자를 LocalDate 타입으로 변경
            val firstDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(startDate.time)
            val firstFormatter = LocalDate.parse(firstDay)
            val lastDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(endDate.time)
            val lastFormatter = LocalDate.parse(lastDay)
            val repeatDate: ArrayList<LocalDate> = arrayListOf()

            //반복시 반복일자로 일정 생성
            if(scheduleData.isRepeated){
                //일정의 반복 주기 받기
                if(scheduleData.repeatType == "day"){
                    for(day in firstFormatter.dayOfMonth .. lastFormatter.dayOfMonth step repeatCycle){
                        repeatDate.add(firstFormatter.plusDays((day - firstFormatter.dayOfMonth).toLong()))
                    }
                }else if(scheduleData.repeatType == "month"){
                    for(month in firstFormatter.month.value .. lastFormatter.month.value step repeatCycle){
                        repeatDate.add(firstFormatter.plusMonths((month-firstFormatter.month.value).toLong()))
                    }
                }else if(scheduleData.repeatType == "text"){
                    for(day in firstFormatter.dayOfMonth .. lastFormatter.dayOfMonth step repeatCycle){
                        repeatDate.add(firstFormatter.plusDays((day - firstFormatter.dayOfMonth).toLong()))
                    }
                }
                //firebase 저장
                saveRepeatData(repeatDate)
            }else{
                //반복 선택이 되지 않은 경우: 일자 수만큼 일정 저장
                for(day in firstFormatter.dayOfMonth .. lastFormatter.dayOfMonth){
                    repeatDate.add(firstFormatter.plusDays((day - firstFormatter.dayOfMonth).toLong()))
                }
                //firebase 저장
                saveRepeatData(repeatDate)
            }

            //CalendarFragment recyclerview 업데이트
            val calendarFragment = (context as MainActivity).pagerAdapter.createFragment(0)
            calendarFragment.onStart()

            //다이얼로그 창 닫기
            dialog.dismiss()
        } else {
            Toast.makeText(context, "일정 이름과 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    //Firebase에 저장
    private fun saveRepeatData(repeatDate: ArrayList<LocalDate>){
        //반복일자대로 일정 생성, firebase 저장
        for(data in repeatDate){
            val scheduleDate = Date.from(data.atStartOfDay(ZoneId.systemDefault()).toInstant())

            val repeatData = ScheduleData(
                Firebase.firebaseAuth.currentUser?.uid.toString(),
                binding.edtScheduleTitle.text.toString(),
                SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(startDate.time),
                SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(endDate.time),
                SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(scheduleDate.time),
                binding.swRepeat.isChecked,
                repeatType,
                repeatCycle,
                binding.edtScheduleDetail.text.toString()
            )

            //데이터 베이스 저장
            firebase.addCalendar(context, repeatData, "일정이 추가되었습니다.")
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
                repeatType = "text"
                repeatCycle = binding.edtRepeatDays.text.toString().toInt()
            }
        }
    }
}