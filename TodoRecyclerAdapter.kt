package com.example.cally

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.cally.databinding.ItemViewBinding
import java.util.*

//todo_view와 RecyclerView를 연결하는 어뎁터
class TodoRecyclerAdapter(var todoDataList: MutableList<TodoData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    //체크 여부 옵션값: 아이템뷰 이동시 필요
    companion object {
        private val ISCHECKED = 0
        private val ISNOTCHECKED = 1
    }
    lateinit var context: Context
    val firebase = Firebase()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as MyViewHolder).binding
        val todoData = todoDataList[position]
        //체크박스 이벤트 초기화
        binding.cbIsChecked.setOnCheckedChangeListener(null)

        binding.tvItemTitle.text = todoData.todoTitle
        binding.cbIsChecked.isChecked = todoData.todoDone

        //todoData 순서 업데이트
        updatePosition()

        //체크 여부에 따라 텍스트 컬러 지정
        if (todoData.todoDone) binding.tvItemTitle.setTextColor(Color.GRAY)
        else binding.tvItemTitle.setTextColor(Color.BLACK)

        //스와이프 후 삭제 이미지 클릭시 해당 아이템뷰 제거
        binding.ivDelete.setOnClickListener {
            removeData(position)
            firebase.deleteTodo(todoData)
        }

        //스와이프 후 수정 이미지 클릭시 해당 아이템뷰 수정 다이얼로그 띄우기
        binding.ivEdit.setOnClickListener {
            val todoEditDialog = TodoEditDialog(context, todoData, this)
            todoEditDialog.showDialog()
        }

        //체크박스 이벤트 등록
        binding.cbIsChecked.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(compoundButton: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    todoData.todoDone = true
                    binding.tvItemTitle.setTextColor(Color.GRAY)
                    moveData(holder.bindingAdapterPosition, todoData, ISCHECKED)
                    firebase.updateTodo(todoData)
                } else {
                    todoData.todoDone = false
                    binding.tvItemTitle.setTextColor(Color.BLACK)
                    moveData(holder.bindingAdapterPosition, todoData, ISNOTCHECKED)
                    firebase.updateTodo(todoData)
                }
            }
        })
    }

    override fun getItemCount(): Int = todoDataList.size

    //아이템뷰 스와이프 중복 제거
    override fun getItemViewType(position: Int): Int = position

    // position 위치의 데이터를 삭제 후 어댑터 갱신
    fun removeData(position: Int) {
        todoDataList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount - position)
    }

    // 현재 선택된 데이터와 드래그한 위치에 있는 데이터를 교환
    fun swapData(fromPos: Int, toPos: Int) {
        todoDataList[fromPos].todoOrder = toPos
        Collections.swap(todoDataList, fromPos, toPos)
        notifyItemMoved(fromPos, toPos)
    }

    //데이터 이동
    fun moveData(fromPos: Int, todoData: TodoData, option: Int) {
        //체크되면 아래로, 체크 해제시 위로 이동
        when (option) {
            ISCHECKED -> {
                todoDataList.add(todoData)
                notifyDataSetChanged()
                notifyItemMoved(fromPos, todoDataList.size - 1)
                todoDataList.removeAt(fromPos)
                todoData.todoOrder = todoDataList.size-1
            }
            ISNOTCHECKED -> {
                todoDataList.add(0, todoData)
                notifyDataSetChanged()
                notifyItemMoved(fromPos + 1, 0)
                todoDataList.removeAt(fromPos + 1)
                todoData.todoOrder = 0
            }
        }
    }

    //todoData 순서 확인
    fun updatePosition(){
        for(data in todoDataList){
            var position = todoDataList.indexOf(data)
            todoDataList[position].todoOrder = position
            firebase.updateTodoOrder(todoDataList[position])
        }
    }
}

//todo_view를 가지는 ViewHolder
class MyViewHolder(val binding: ItemViewBinding): RecyclerView.ViewHolder(binding.root)