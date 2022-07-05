package com.example.cally

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cally.databinding.FragmentFriendsBinding

//친구 목록 불러오기
class FriendsFragment : Fragment() {
    lateinit var binding: FragmentFriendsBinding
    lateinit var friendRecyclerAdapter: FriendRecyclerAdapter
    var friendList: MutableList<FriendData> = mutableListOf()
    private val FRIEND_FRAGMENT = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendsBinding.inflate(inflater, container, false)

        friendRecyclerAdapter = FriendRecyclerAdapter(friendList, FRIEND_FRAGMENT)
        binding.friendsRecyclerView.adapter = friendRecyclerAdapter
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(context)

        //친구 목록 생성
        selectFriendList(friendRecyclerAdapter)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        selectFriendList(friendRecyclerAdapter)
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
                var friendList: MutableList<FriendData> = mutableListOf()
                if (result.documents.isNotEmpty()) {
                    val groupData = result.documents[0].toObject(GroupData::class.java)

                    //userId 기준으로 Friend 생성
                    if (groupData != null) {
                        Firebase.db.collection("Group")
                            .document(groupData.docId.toString())
                            .collection("Friend")
                            .get()
                            .addOnSuccessListener { result ->
                                //생성된 UserData를 friendList에 추가
                                for (document in result) {
                                    val friendData = document.toObject(FriendData::class.java)
                                    friendList.add(friendData)
                                }

                                friendRecyclerAdapter.friendList.clear()
                                friendRecyclerAdapter.friendList.addAll(friendList)
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
            binding.tvEmptyFriend.visibility = View.VISIBLE
            binding.friendsRecyclerView.visibility = View.INVISIBLE
            binding.tvText.visibility = View.INVISIBLE
        }else{
            binding.tvEmptyFriend.visibility = View.INVISIBLE
            binding.friendsRecyclerView.visibility = View.VISIBLE
            binding.tvText.visibility = View.VISIBLE
        }
    }
}