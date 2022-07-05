package com.example.cally

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cally.databinding.ItemViewBinding
import java.util.*

//일정과 CalendarFragment를 연결하는 어뎁터
class CalendarRecyclerAdapter(var scheduleDataList: MutableList<ScheduleData>, val selectedDate: Calendar): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object{
        private val EDIT_CALENDAR = 0
        private val SHOW_CALENDAR = 1
    }
    lateinit var context: Context
    val firebase = Firebase()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as CalendarViewHolder).binding
        val scheduleData = scheduleDataList[position]
        //체크박스 숨기기
        binding.cbIsChecked.visibility = View.INVISIBLE
        //이름 바꾸기
        binding.tvItemTitle.text = scheduleData.scheduleTitle

        //스와이프 후 삭제 이미지 클릭시 해당 아이템뷰 제거
        binding.ivDelete.setOnClickListener {
            removeData(position)
            firebase.deleteSchedule(scheduleData)

            val calendarFragment = (context as MainActivity).pagerAdapter.createFragment(0)
            calendarFragment.onStart()
        }

        //스와이프 후 수정 이미지 클릭시 해당 아이템뷰 수정
        binding.ivEdit.setOnClickListener {
            val calendarEditDialog = CalendarEditDialog(context, scheduleData, selectedDate, EDIT_CALENDAR)
            calendarEditDialog.showDialog()
        }

        //itemView 클릭 이벤트 등록: 일정 상세정보 확인
        binding.swipeView.setOnClickListener {
            val calendarEditDialog = CalendarEditDialog(context, scheduleData, selectedDate, SHOW_CALENDAR)
            calendarEditDialog.showDialog()
        }
    }

    override fun getItemCount(): Int = scheduleDataList.size

    //아이템뷰 스와이프 중복 제거
    override fun getItemViewType(position: Int): Int = position

    // position 위치의 데이터를 삭제 후 어댑터 갱신
    private fun removeData(position: Int) {
        scheduleDataList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }

    // 현재 선택된 데이터와 드래그한 위치에 있는 데이터를 교환
    fun swapData(fromPos: Int, toPos: Int) {
        Collections.swap(scheduleDataList, fromPos, toPos)
        notifyItemMoved(fromPos, toPos)
    }

}

class CalendarViewHolder(val binding: ItemViewBinding): RecyclerView.ViewHolder(binding.root)
