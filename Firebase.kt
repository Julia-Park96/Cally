package com.example.cally

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.multidex.MultiDexApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class Firebase: MultiDexApplication() {
    companion object{
        lateinit var firebaseAuth: FirebaseAuth

        //파이어베이스 객체 참조변수
        @SuppressLint("StaticFieldLeak")
        lateinit var db: FirebaseFirestore

        //userData
        var userData = UserData()
    }

    override fun onCreate() {
        super.onCreate()
        firebaseAuth = Firebase.auth

        //파이어베이스 데이터베이스 객체 생성
        db = FirebaseFirestore.getInstance()
    }

    //UserData 추가
    fun addUser(){
        //사용자 정보 받기
        val currentUser = firebaseAuth.currentUser

        db.collection("User")
            .whereEqualTo("userId", currentUser?.uid)
            .get()
            .addOnSuccessListener { result ->
                //사용자정보 미생성시 생성
                if(result.documents.isEmpty()){
                    //User 클래스 생성 및 데이터베이스에 유저정보 저장
                    val userData = UserData(currentUser?.uid!!, currentUser.email!!)
                    val user = mapOf(
                        "userId" to userData.userId,
                        "userEmail" to userData.userEmail,
                    )

                    db.collection("User")
                        .add(user)
                        .addOnSuccessListener {
                            Log.d("Hwang", "UserData 생성 성공")
                        }
                        .addOnFailureListener {
                            Log.d("Hwang", "UserData 생성 실패: $it")
                        }
                }

            }
            .addOnFailureListener {
                Log.d("Hwang", "userId 가져오기 실패: $it")
            }
    }

    //TodoData 추가
    @SuppressLint("NotifyDataSetChanged")
    fun addTodo(context: Context, todoData: TodoData) {
        //db 필드명과 todoData 맵핑
        val todo = mapOf(
            "userId" to todoData.userId,
            "todoTitle" to todoData.todoTitle,
            "todoDate" to todoData.todoDate,
            "todoDone" to todoData.todoDone,
            "todoOrder" to todoData.todoOrder
        )

        //파이어스토어에 할일 항목 추가
        db.collection("Todo")
            .add(todo)
            .addOnSuccessListener {
                //할일 추가 완료 안내
                Toast.makeText(context, "할 일이 추가되었습니다.", Toast.LENGTH_SHORT).show()
                //TodoData 클래스에 문서 ID 저장
                it.update("docId", it.id)
            }
            .addOnFailureListener {
                //할일 추가 실패 안내
                Toast.makeText(context, "할 일 저장에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                Log.d("Hwang", "할일 저장 실패: $it")
            }
    }

    //ScheduleData 추가
    fun addCalendar(context: Context, scheduleData: ScheduleData, text: String){
        //db 필드명과 scheduleData 맵핑
        val schedule = mapOf(
            "userId" to scheduleData.userId,
            "scheduleTitle" to scheduleData.scheduleTitle,
            "startDate" to scheduleData.startDate,
            "endDate" to scheduleData.endDate,
            "scheduleDate" to scheduleData.scheduleDate,
            "isRepeated" to scheduleData.isRepeated,
            "repeatType" to scheduleData.repeatType,
            "repeatCycle" to scheduleData.repeatCycle,
            "scheduleDetail" to scheduleData.scheduleDetail
        )

        //파이어스토어에 할일 항목 추가
        db.collection("Schedule")
            .add(schedule)
            .addOnSuccessListener {
                //일정 추가 완료 안내
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                //ScheduleData와 FireStore에 문서 ID 저장
                scheduleData.docId = it.id
                it.update("docId", scheduleData.docId)
            }
            .addOnFailureListener {
                //일정 추가 실패 안내
                Log.d("Hwang", "일정 추가 실패: $it")
            }
    }

    //해당 일자 할일 목록 받기
    @SuppressLint("NotifyDataSetChanged")
    fun selectTodoList(todoRecyclerAdapter: TodoRecyclerAdapter, selectedDate: Date) {
        todoRecyclerAdapter.todoDataList.clear()

        db.collection("Todo")
            .whereIn("userId", mutableListOf(firebaseAuth.currentUser?.uid))
            .whereEqualTo("todoDate", SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(selectedDate.time))
            .orderBy("todoOrder", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                for(document in result){
                    val todo = document.toObject(TodoData::class.java)
                    todoRecyclerAdapter.todoDataList.add(todo)
                    todoRecyclerAdapter.notifyDataSetChanged()
                }
                todoRecyclerAdapter.notifyDataSetChanged()
                Log.d("Hwang", "할일 목록 가져오기 성공")
            }
            .addOnFailureListener {
                Log.d("Hwang", "할일 목록 가져오기 실패: $it")
            }
    }

    //해당 일자 일정 목록 받기
    @SuppressLint("NotifyDataSetChanged")
    fun selectScheduleList(calendarRecyclerAdapter: CalendarRecyclerAdapter, selectedDate: Date) {
        calendarRecyclerAdapter.scheduleDataList.clear()

        db.collection("Schedule")
            .whereIn("userId", mutableListOf(firebaseAuth.currentUser?.uid))
            .whereEqualTo("scheduleDate", SimpleDateFormat("yyyy-MM-dd-E", Locale.KOREA).format(selectedDate.time))
            .get()
            .addOnSuccessListener { result ->
                for(document in result){
                    val schedule = document.toObject(ScheduleData::class.java)
                    calendarRecyclerAdapter.scheduleDataList.add(schedule)
                }
                calendarRecyclerAdapter.notifyDataSetChanged()
                Log.d("Hwang", "일정 목록 가져오기 성공")
            }
            .addOnFailureListener {
                Log.d("Hwang", "일정 목록 가져오기 실패: $it")
            }
    }

    //todoData 수정
    fun updateTodo(todoData: TodoData) {
        db.collection("Todo").document(todoData.docId!!)
            .set(todoData)
            .addOnSuccessListener {
                Log.d("Hwang", "todo 수정 성공")
            }
            .addOnFailureListener {
                Log.d("Hwang", "todo 수정 실패: $it")
            }
    }

    //할일 목록 위치 업데이트
    fun updateTodoOrder(todoData: TodoData){
        if(todoData.docId != null){
            db.collection("Todo")
                .document(todoData.docId.toString())
                .update("todoOrder", todoData.todoOrder)
                .addOnSuccessListener {
                    Log.d("Hwang", "todo 순서 업데이트 성공")
                }
                .addOnFailureListener {
                    Log.d("Hwang", "todo 순서 업데이트 실패: $it")
                }
        }
    }

    //scheduleData 수정
    fun updateSchedule(scheduleData: ScheduleData) {
        db.collection("Schedule").document(scheduleData.docId!!)
            .set(scheduleData)
            .addOnSuccessListener {
                Log.d("Hwang", "schedule 수정 성공")
            }
            .addOnFailureListener {
                Log.d("Hwang", "schedule 수정 실패: $it")
            }
    }

    //userData 수정
    fun updateFriend(friendData: FriendData) {
        val currentUser = firebaseAuth.currentUser
        db.collection("Group")
            .whereEqualTo("userId", currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                if(it.documents.isNotEmpty()){
                    val docId = it.documents[0].id

                    db.collection("Group")
                        .document(docId)
                        .collection("Friend")
                        .document(friendData.docId.toString())
                        .set(friendData)
                        .addOnSuccessListener {
                            Log.d("Hwang", "friend 수정 성공")
                        }
                        .addOnFailureListener {
                            Log.d("Hwang", "friend 수정 실패: $it")
                        }
                }
            }
            .addOnFailureListener {
                Log.d("Hwang", "user 수정 실패: $it")
            }
    }

    //공유 친구 목록 초기화
    fun initializingFriend(userId: String){
        db.collection("Group")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                if(result.documents.isNotEmpty()){
                    val docId = result.documents[0].id
                    db.collection("Group")
                        .document(docId)
                        .collection("Friend")
                        .get()
                        .addOnSuccessListener {
                            for(document in it){
                                val friendData = document.toObject<FriendData>()
                                db.collection("Group")
                                    .document(docId)
                                    .collection("Friend")
                                    .document(friendData.docId.toString())
                                    .update("isChecked", false)
                                    .addOnSuccessListener {
                                        Log.d("Hwang", "공유 친구 목록 초기화 완료")
                                    }
                                    .addOnFailureListener {
                                        Log.d("Hwang", "공유 친구 목록 초기화 실패: $it")
                                    }
                            }
                        }
                        .addOnFailureListener {
                            Log.d("Hwang", "공유 친구 목록 초기화 실패: $it")
                        }
                }
            }
            .addOnFailureListener {
                Log.d("Hwang", "공유 친구 목록 초기화 실패: $it")
            }
    }

    //todoData 삭제
    fun deleteTodo(todoData: TodoData){
        db.collection("Todo").document(todoData.docId!!)
            .delete()
            .addOnSuccessListener {
                Log.d("Hwang", "todo 삭제 성공")
            }
            .addOnFailureListener {
                Log.d("Hwang", "todo 삭제 실패: $it")
            }
    }

    //scheduleData 삭제
    fun deleteSchedule(scheduleData: ScheduleData) {
        db.collection("Schedule").document(scheduleData.docId!!)
            .delete()
            .addOnSuccessListener {
                Log.d("Hwang", "schedule 삭제 성공")
            }
            .addOnFailureListener {
                Log.d("Hwang", "schedule 삭제 실패: $it")
            }
    }

    //GroupData 생성
    fun addGroup(userId: String, userData: UserData, context: Context) {
        Log.d("Hwang", "$userId, $userData")

        val friend = mapOf(
            "friendId" to userData.userId,
            "friendEmail" to userData.userEmail,
            "isChecked" to false
        )

        db.collection("Group")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                //기존 친구 목록이 없는 경우: GroupData 생성
                if (result.documents.isEmpty()) {
                    val group = mapOf(
                        "userId" to userId
                    )

                    //그룹 데이터 생성
                    db.collection("Group")
                        .add(group)
                        .addOnSuccessListener { result ->
                            //문서 id 넣기
                            result.update("docId", result.id)
                            //하위 컬랙션 Friend 생성
                            db.collection("Group")
                                .document(result.id)
                                .collection("Friend")
                                .add(friend)
                                .addOnSuccessListener {
                                    it.update("docId", it.id)
                                    Log.d("Hwang", "Friend 데이터 추가 성공")
                                    Toast.makeText(context, "친구 추가에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Log.d("Hwang", "친구 추가 실패: $it")

                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "친구 추가에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                            Log.d("Hwang", "친구 추가 실패: $it")

                        }
                }else{
                    //기존 친구 목록이 있는 경우: userIds에 추가
                    val groupDocId = result.documents[0].get("docId").toString()

                    //친구 중복 확인
                    db.collection("Group")
                        .document(groupDocId)
                        .collection("Friend")
                        .whereEqualTo("friendId", userData.userId)
                        .get()
                        .addOnSuccessListener {
                            //친구 등록 여부 확인
                            if(it.documents.isEmpty()){
                                //등록되어 있지 않다면 friendId 추가
                                db.collection("Group")
                                    .document(groupDocId)
                                    .collection("Friend")
                                    .add(friend)
                                    .addOnSuccessListener {
                                        it.update("docId", it.id)
                                        Log.d("Hwang", "Friend 데이터 추가 성공")
                                        Toast.makeText(context, "친구 추가에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "친구 추가에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                                        Log.d("Hwang", "친구 추가 실패: $it")
                                    }
                            }else{
                                //등록된 경우 알림
                                Toast.makeText(context, "이미 등록된 친구입니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Log.d("Hwang", "친구 추가 실패: $it")
                        }
                }
            }
            .addOnFailureListener {
                Log.d("Hwang", "친구 추가 실패: $it")
            }
    }
}
