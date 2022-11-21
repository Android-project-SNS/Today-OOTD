package com.example.today_ootd.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.today_ootd.R
import com.example.today_ootd.databinding.FragmentMypageBinding
import com.example.today_ootd.model.FollowModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
* A simple [Fragment] subclass.
* Use the [MypageFragment.newInstance] factory method to
* create an instance of this fragment.
*/
class MypageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var uid : String? = null
    private var currentUid : String? = null
    private lateinit var firestore : FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private var binding : FragmentMypageBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firestore = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        binding = FragmentMypageBinding.inflate(inflater, container, false)

        currentUid = auth.currentUser!!.uid
        uid = arguments?.getString("destinationUid")

        binding!!.accountBtnFollowSignout.setOnClickListener {

        }
        if (uid == currentUid){
            // My Page

        }
        else {
            // Other User Page
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mypage, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MypageFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MypageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // 팔로우 기능
    fun requestFollow(){
        var tsDocFollowing = firestore?.collection("users")?.document(currentUid!!)
        firestore?.runTransaction { transaction ->
            var followModel = transaction.get(tsDocFollowing!!).toObject(FollowModel::class.java)
            if (followModel == null){
                followModel = FollowModel()
                followModel!!.followingCount = 1
                followModel!!.followers[uid!!] = true

                transaction.set(tsDocFollowing, followModel)
                return@runTransaction
            }

            // 이미 팔로우한 유저라면 (취소)
            if (followModel.following.containsKey(uid)){
                followModel?.followingCount = followModel?.followingCount?.minus(1)!!
                followModel?.followers?.remove(uid)
            }
            // 팔로우 하고 있지 않다면 (팔로우)
            else{
                followModel?.followingCount = followModel?.followingCount?.plus(1)!!
                followModel?.following?.set(uid!!, true)
            }
            transaction.set(tsDocFollowing, followModel)
            return@runTransaction
        }

        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followModel = transaction.get(tsDocFollower!!).toObject(FollowModel::class.java)
            if (followModel == null) {
                followModel = FollowModel()
                followModel!!.followerCount = 1
                followModel!!.following[currentUid!!] = true

                transaction.set(tsDocFollower, followModel!!)
                return@runTransaction
            }
            if (followModel!!.followers.containsKey(currentUid)){
                // 팔로우 하고 있는 경우
                followModel!!.followerCount = followModel!!.followerCount - 1
                followModel!!.followers.remove(currentUid)
            }
            else {
                // 팔로우 하고 있지 않은 경우
                followModel!!.followerCount = followModel!!.followerCount + 1
                followModel!!.followers[currentUid!!] = true
            }
            transaction.set(tsDocFollower, followModel!!)
            return@runTransaction
        }
    }
}