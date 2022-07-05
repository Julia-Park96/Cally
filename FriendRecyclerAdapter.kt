package com.example.cally

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.cally.databinding.FriendsViewBinding

class FriendRecyclerAdapter(val friendList: MutableList<FriendData>, val option: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object{
        private val FRIEND_FRAGMENT = 0
        private val FRIEND_DIALOG = 1
    }
    lateinit var binding: FriendsViewBinding
    private val firebase = Firebase()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        binding = FriendsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        //체크박스 이벤트 초기화
        binding.cbIsChosen.setOnCheckedChangeListener(null)

        val friendData = friendList.get(position)
        binding.tvName.text = friendData.friendEmail

        when(option){
            FRIEND_FRAGMENT -> {
                //체크박스 숨기기
                binding.cbIsChosen.visibility = View.INVISIBLE
            }
            FRIEND_DIALOG -> {
                //체크박스 드러내기
                binding.cbIsChosen.visibility = View.VISIBLE
                //체크박스 이벤트 설정
                binding.cbIsChosen.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
                    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                        if(isChecked){
                            friendData.isChecked = true
                            firebase.updateFriend(friendData)
                        }else{
                            friendData.isChecked = false
                            firebase.updateFriend(friendData)
                        }
                    }
                })
            }
        }
    }

    override fun getItemCount(): Int = friendList.size
}

class FriendViewHolder(val binding: FriendsViewBinding): RecyclerView.ViewHolder(binding.root)