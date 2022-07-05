package com.example.cally

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cally.databinding.FriendDialogBinding
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class FriendDialog(val context: Context, var scheduleData: ScheduleData): View.OnClickListener {
    lateinit var binding: FriendDialogBinding
    lateinit var friendRecyclerAdapter: FriendRecyclerAdapter

    private var friendList: MutableList<FriendData> = mutableListOf()
    private val FRIEND_DIALOG = 1
    private val firebase = Firebase()

    private val dialog = Dialog(context)

    fun showDialog() {
        binding = FriendDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        //다이얼로그 사이즈 지정
        val params: WindowManager.LayoutParams = dialog.window!!.attributes
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        params.height = WindowManager.LayoutParams.MATCH_PARENT

        //recycler adapter 등록
        friendRecyclerAdapter = FriendRecyclerAdapter(friendList, FRIEND_DIALOG)
        binding.friendDialogRecyclerView.adapter = friendRecyclerAdapter
        binding.friendDialogRecyclerView.layoutManager = LinearLayoutManager(context)

        //친구 목록 받기
        selectFriendList(friendRecyclerAdapter)
        dialog.show()

        //이벤트 등록
        binding.ivFrieondClose.setOnClickListener(this)
        binding.ivFriendSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            //다이얼로그 종료
            R.id.ivFrieondClose -> {
                dialog.dismiss()
            }
            //공유된 친구, 현재 유저에 일정 추가
            R.id.ivFriendSave -> {
                addSharedFriend()
            }
        }
    }

    //친구 목록 받기
    @SuppressLint("NotifyDataSetChanged")
    private fun selectFriendList(friendRecyclerAdapter: FriendRecyclerAdapter) {
        val currentUser = Firebase.firebaseAuth.currentUser

        //userId의 정보와 같은 GroupData 여부 확인
        Firebase.db.collection("Group")
            .whereEqualTo("userId", currentUser!!.uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.documents.isNotEmpty()) {
                    val groupData = result.documents[0].toObject(GroupData::class.java)

                    //groupData의 userIds 저장
                    if (groupData != null) {
                        //userId 기준으로 FriendData 생성
                        Firebase.db.collection("Group")
                            .document(groupData.docId.toString())
                            .collection("Friend")
                            .get()
                            .addOnSuccessListener { result ->
                                friendRecyclerAdapter.friendList.clear()
                                for (document in result) {
                                    val friendData = document.toObject(FriendData::class.java)
                                    friendRecyclerAdapter.friendList.add(friendData)
                                }

                                friendRecyclerAdapter.notifyDataSetChanged()
                                setView()
                                Log.d("Hwang", "친구 목록 받기 성공")
                            }
                            .addOnFailureListener {
                                Log.d("Hwang", "친구 목록 받기 실패")
                            }
                    }
                } else {
                    Log.d("Hwang", "GroupData 받기 실패")
                }
            }
            .addOnFailureListener {
                Log.d("Hwang", "친구 목록 가져오기 실패")
            }
    }

    //친구 목록이 비어있다면 텍스트만 보이기
    private fun setView(){
        if(friendList.size<=0){
            binding.tvEmptyDialog.visibility = View.VISIBLE
            binding.friendDialogRecyclerView.visibility = View.INVISIBLE
            binding.tvText2.visibility = View.INVISIBLE
        }else{
            binding.tvEmptyDialog.visibility = View.INVISIBLE
            binding.friendDialogRecyclerView.visibility = View.VISIBLE
            binding.tvText2.visibility = View.VISIBLE
        }
    }

    //체크된 친구 일정 추가
    private fun addSharedFriend(){
        val currentUser = Firebase.firebaseAuth.currentUser

        Firebase.db.collection("Group")
            .whereEqualTo("userId", currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                val groupData = it.documents[0].toObject(GroupData::class.java)
                Firebase.db.collection("Group")
                    .document(groupData?.docId.toString())
                    .collection("Friend")
                    .whereEqualTo("isChecked", true)
                    .get()
                    .addOnSuccessListener { result ->
                        for(document in result){
                            val friendData = document.toObject(FriendData::class.java)
                            scheduleData.userId = friendData.friendId
                            saveCalendar(scheduleData)
                        }

                        //공유된 Friend isChecked 초기화
                        firebase.initializingFriend(currentUser.uid)

                        //현재 유저 일정 저장
                        scheduleData.userId = currentUser.uid
                        saveCalendar(scheduleData)

                        //CalendarFragment recyclerview 업데이트
                        val calendarFragment = (context as MainActivity).pagerAdapter.createFragment(0)
                        calendarFragment.onStart()

                        //다이얼로그 창 닫기
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Log.d("Hwang", "selectedFriend 가져오기 실패 $it")
                    }
            }
            .addOnFailureListener {
                Log.d("Hwang", "groupData 가져오기 실패 $it")
            }
    }

    //일정 추가
    private fun saveCalendar(scheduleData: ScheduleData){
        //시작일자, 종료일자를 LocalDate 타입으로 변경
        val firstDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateFormatter(scheduleData.startDate))
        val firstFormatter = LocalDate.parse(firstDay)
        val lastDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dateFormatter(scheduleData.endDate))
        val lastFormatter = LocalDate.parse(lastDay)
        val repeatDate: ArrayList<LocalDate> = arrayListOf()

        //반복시 반복일자로 일정 생성
        if(scheduleData.isRepeated){
            //일정의 반복일자 받기
            if(scheduleData.repeatType == "day"){
                for(day in firstFormatter.dayOfMonth .. lastFormatter.dayOfMonth step scheduleData.repeatCycle){
                    repeatDate.add(firstFormatter.plusDays((day - firstFormatter.dayOfMonth).toLong()))
                }
            }else if(scheduleData.repeatType == "month"){
                for(month in firstFormatter.month.value .. lastFormatter.month.value step scheduleData.repeatCycle){
                    repeatDate.add(firstFormatter.plusMonths((month-firstFormatter.month.value).toLong()))
                }
            }else if(scheduleData.repeatType == "text"){
                for(day in firstFormatter.dayOfMonth .. lastFormatter.dayOfMonth step scheduleData.repeatCycle){
                    repeatDate.add(firstFormatter.plusDays((day - firstFormatter.dayOfMonth).toLong()))
                }
            }
            //firebase 저장
            saveRepeatData(scheduleData, repeatDate)
        }else {
            //일자 수만큼 일정 저장
            for (day in firstFormatter.dayOfMonth..lastFormatter.dayOfMonth) {
                repeatDate.add(firstFormatter.plusDays((day - firstFormatter.dayOfMonth).toLong()))
            }
            //firebase 저장
            saveRepeatData(scheduleData, repeatDate)
        }
    }

    //Firebase에 저장
    private fun saveRepeatData(scheduleData: ScheduleData, repeatDate: ArrayList<LocalDate>){
        //반복일자대로 일정 생성, firebase 저장
        for(data in repeatDate){
            val scheduleDate = Date.from(data.atStartOfDay(ZoneId.systemDefault()).toInstant())
            scheduleData.scheduleDate = SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(scheduleDate.time)
            firebase.addCalendar(context, scheduleData, "일정이 공유되었습니다.")
        }
    }

    //날짜 형식 변경: 데이터베이스의 문자열 날짜를 Date 형식으로 변경
    private fun dateFormatter(date: String): Date {
        val token = date.split('-')
        val year = token[0].toInt()
        val month = token[1].toInt()
        val day = token[2].toInt()
        val convertedDate: Calendar = Calendar.getInstance()
        convertedDate.set(year, month-1 , day)

        return convertedDate.time
    }
}