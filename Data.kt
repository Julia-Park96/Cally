package com.example.cally

import com.google.firebase.firestore.PropertyName
import java.util.*

//사용자 데이터 클래스
data class UserData(var userId: String = "",        //firebase UID
                    var userEmail: String = "")     //가입시 입력된 user email

//일정 데이터 클래스
data class ScheduleData(var userId: String = "",                                            //firebase UID
                        var scheduleTitle: String = "",                                     //일정 제목
                        var startDate: String = Calendar.getInstance().time.toString(),     //일정 시작일
                        var endDate: String = Calendar.getInstance().time.toString(),       //일정 종료일
                        var scheduleDate: String = Calendar.getInstance().time.toString(),  //일정일
                        @get:PropertyName("isRepeated")     //boolean 값이 true일 때 Firebase 필드명의 is가 사라지는 오류 방지
                        @set:PropertyName("isRepeated")
                        var isRepeated: Boolean = false,    //반복여부
                        var repeatType: String = "",        //반복주기 타입
                        var repeatCycle: Int = 0,           //반복주기 값
                        var scheduleDetail: String? = "",   //일정 상세
                        var docId: String? = null)          //문서 ID

//할일 목록 데이터 클래스
data class TodoData(var userId: String = "",                                        //firebase UID
                    var todoTitle: String = "",                                     //할일 제목
                    var todoDate: String = Calendar.getInstance().time.toString(),  //할일 일자
                    var todoDone: Boolean = false,                                  //할일 수행 여부
                    var todoOrder: Int = 0,                                         //할일 순서
                    var docId: String? = null)                                      //문서 ID

//친구 목록 데이터 클래스
data class GroupData(var userId: String = "",                           //firebase UID
                     var userIds: ArrayList<String> = arrayListOf(),    //친구목록 사용자 UID
                     var docId: String? = null)                         //문서 ID

//친구 데이터 클래스
data class FriendData(var friendId: String = "",        //친구 firebase UID
                      var friendEmail: String = "",     //친구 email
                      @get:PropertyName("isChecked")    //boolean 값이 true일 때 Firebase 필드명의 is가 사라지는 오류 방지
                      @set:PropertyName("isChecked")
                      var isChecked: Boolean = false,   //일정 공유 대상 여부
                      var docId: String? = null)        //문서 ID