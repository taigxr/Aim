package com.example.aim

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aim.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class MainActivity : AppCompatActivity() {

    private var usernameList = mutableListOf<String>()
    private var captionsList = mutableListOf<String>()
    private var imageList = mutableListOf<Int>()
    private var pfpList = mutableListOf<Int>()
    val currentUser = Firebase.auth.currentUser
    val uid = currentUser?.uid

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postToFeed()

        val recyclerView = findViewById<RecyclerView>(R.id.feed_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecyclerAdapter(usernameList, captionsList, imageList, pfpList)

        binding.bottomBar.homeButton.setOnClickListener{

        }
        binding.bottomBar.calenderButton.setOnClickListener{

        }
        binding.bottomBar.cameraButton.setOnClickListener{

        }
        binding.bottomBar.socialButton.setOnClickListener{
            val intent = Intent(this, FindFriendsActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.bottomBar.userButton.setOnClickListener{
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun addToFeed(username: String, caption: String, image: Int, pfp: Int) {
        usernameList.add(username)
        captionsList.add(caption)
        imageList.add(image)
        pfpList.add(pfp)
    }

    private fun postToFeed() {
        for (i in 2..25) {
            addToFeed(
                "TESTUsername $i",
                "Hello this is a post",
                R.drawable.img_8930,
                R.drawable.ic_launcher_background


                //for # of friends in friends list
                //grab most recent picture, user name, caption from most recent post, and user pfp
                //add to feed
            )
        }
    }

}









