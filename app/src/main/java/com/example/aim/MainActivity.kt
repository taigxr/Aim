package com.example.aim

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aim.databinding.ActivityMainBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val usernameList = mutableListOf<String>()
    private val captionsList = mutableListOf<String>()
    private val timestampsList = mutableListOf<String>()  // formatted strings
    private val imageUrlList = mutableListOf<String>()
    private val pfpUrlList = mutableListOf<String>()

    private lateinit var adapter: RecyclerAdapter
    private lateinit var binding: ActivityMainBinding

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RecyclerAdapter(usernameList, captionsList, imageUrlList, pfpUrlList, timestampsList)
        val recyclerView = findViewById<RecyclerView>(R.id.feed_rv)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

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

        loadFriendsPosts()
    }

    private fun addToFeed(
        username: String,
        caption: String,
        timestamp: Timestamp,
        imageUrl: String,
        pfpUrl: String
    ) {
        usernameList.add(username)
        captionsList.add(caption)

        // Format timestamp to readable string, e.g. "Jul 15, 2025 8:00 PM"
        val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        timestampsList.add(sdf.format(timestamp.toDate()))

        imageUrlList.add(imageUrl)
        pfpUrlList.add(pfpUrl)
    }

    private fun loadFriendsPosts() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId).get()
            .addOnSuccessListener { userDoc ->
                val friends = userDoc.get("friends") as? List<String> ?: emptyList()

                if (friends.isEmpty()) {
                    Toast.makeText(this, "No friends found.", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val chunks = friends.chunked(10)
                usernameList.clear()
                captionsList.clear()
                timestampsList.clear()
                imageUrlList.clear()
                pfpUrlList.clear()

                for (chunk in chunks) {
                    db.collection("posts")
                        .whereIn("userId", chunk)
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { postsSnapshot ->
                            for (postDoc in postsSnapshot.documents) {
                                val imageUrl = postDoc.getString("imageUrl") ?: continue
                                val caption = postDoc.getString("caption") ?: ""
                                val username = postDoc.getString("username") ?: "Unknown"
                                val timestamp = postDoc.getTimestamp("timestamp") ?: Timestamp.now()
                                val pfpUrl = postDoc.getString("pfpUrl") ?: ""

                                addToFeed(username, caption, timestamp, imageUrl, pfpUrl)
                            }
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to load posts: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load friends list: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Example upload post function (you'll call this when posting)
    private fun uploadPost(imageUri: Uri, caption: String) {
        val userId = auth.currentUser?.uid ?: return
        val username = auth.currentUser?.displayName ?: "Anonymous"
        val pfpUrl = auth.currentUser?.photoUrl?.toString() ?: "" // or fetch from user doc

        val storageRef = storage.reference.child("posts/$userId/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    val post = hashMapOf(
                        "username" to username,
                        "caption" to caption,
                        "timestamp" to Timestamp.now(),
                        "imageUrl" to downloadUri.toString(),
                        "pfpUrl" to pfpUrl,
                        "userId" to userId
                    )
                    db.collection("posts")
                        .add(post)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Post uploaded successfully", Toast.LENGTH_SHORT).show()
                            loadFriendsPosts() // reload feed after posting
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to add post to DB: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
            }
    }
}
