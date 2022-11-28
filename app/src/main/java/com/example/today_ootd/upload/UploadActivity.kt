package com.example.today_ootd.upload

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.Data
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.today_ootd.R
import com.example.today_ootd.databinding.ActivityUploadBinding
import com.example.today_ootd.home.HomeFragment
import com.example.today_ootd.model.ArticleModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.snapshots
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

class UploadActivity : AppCompatActivity(),AdapterView.OnItemSelectedListener {
    lateinit var binding: ActivityUploadBinding
    val homeFragment = HomeFragment()
    val db : FirebaseFirestore = Firebase.firestore
    val itemsCollectionRef = db.collection("OOTD")
    private var selectedUri: Uri? = null
    private val auth: FirebaseAuth by lazy {
        Firebase.auth
    }
    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private val articleDB: DatabaseReference by lazy {
        Firebase.database.reference.child("OOTD")
    }
    private val userDB:DatabaseReference by lazy{
        Firebase.database.reference.child("user")
    }
    private var currentUid : String? = auth!!.currentUser!!.uid
    private var nickname : String = ""
    private var height : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_upload)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addImageButton.setOnClickListener {
            startContentProvider()
            when {
                checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    startContentProvider()
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }
                else -> {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1010)
                }
            }
        }

        val spinner: Spinner = findViewById(R.id.spinner)
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        binding.submitButton.setOnClickListener {
            val outer = binding.myOuter.text.toString()
            val weather = binding.todayTemperature.text.toString()
            val sellerId = auth.currentUser?.uid.orEmpty()
            val top = binding.myTop.text.toString()
            val bottom = binding.myBottom.text.toString()
            val shoes = binding.myShoes.text.toString()
            val bag = binding.myBag.text.toString()
            val acc = binding.myAcc.text.toString()
            //var nickname: String
            val style = binding.spinner.selectedItem.toString()

            userDB.child(currentUid!!).addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    nickname = snapshot.child("nickname").value.toString()
                    height = snapshot.child("height").value.toString().toInt()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
            println("############################닉네임은???: "+nickname)

            //Log.d("nickname",nickname)
            showProgress()

            // 중간에 이미지가 있으면 업로드 과정을 추가
            if (selectedUri != null) {
                val photoUri = selectedUri ?: return@setOnClickListener
                uploadPhoto(photoUri,
                    successHandler = { uri ->
                        uploadArticle(sellerId, outer,top,bottom,shoes,bag,acc,weather,uri,nickname,style,height)
                    },
                    errorHandler = {
                        Toast.makeText(this, "사진 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        hideProgress()
                    }
                )
            } else {
                uploadArticle(sellerId, outer,top,bottom,shoes,bag,acc,weather, "", nickname, style,height)
            }
            //replaceFragment(homeFragment)
        }
    }

    private fun replaceFragment(fragment : Fragment) {
        Log.d("MainActivity","${fragment}")
        supportFragmentManager.beginTransaction()
            .apply {
                replace(R.id.fragmentCotainer,fragment)
                commit()
            }
    }
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }

    //storage에 사진 업로드 함수
    private fun uploadPhoto(uri: Uri, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        storage.reference.child("OOTD/photo").child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    storage.reference.child("OOTD/photo").child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }.addOnFailureListener {
                            errorHandler()
                        }
                } else {
                    errorHandler()
                }
            }
    }

    //DB에 글 업로드 함수
    private fun uploadArticle(sellerId: String, outer: String,top:String,bottom:String,shoes:String,bag:String,acc:String,weather: String, imageUrl: String, nickname: String, style:String, height:Int) {
        val like = mutableMapOf<String, Boolean>()
        val model = ArticleModel(sellerId,outer, top, bottom, shoes, bag, acc, System.currentTimeMillis(),
            "$weather ℃", imageUrl, nickname, style, height, like)
        articleDB.push().setValue(model)

        hideProgress()
        finish()
    }

    private fun startContentProvider() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 2020)
    }

    private fun showProgress() {
        binding.progressBar.isVisible = true
    }

    private fun hideProgress() {
        binding.progressBar.isVisible = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            2020 -> {
                val uri = data?.data
                if (uri != null) {
                    binding.addImageButton.setImageURI(uri)
                    selectedUri = uri
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }

            }
            else -> {
                Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

        private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("사진을 가져오기 위해 필요합니다.")
            .setPositiveButton("동의") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1010)
            }
            .create()
            .show()
    }


}