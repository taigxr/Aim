package com.example.aim

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyStreakActivity : AppCompatActivity() {

    private lateinit var streakTextView: TextView
    private val db = Firebase.firestore
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_streak)

        streakTextView = findViewById(R.id.streakTextView)

        if (currentUserId.isNotEmpty()) {
            db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener { document ->
                    val streak = (document.getLong("workoutStreak") ?: 0).toInt()
                    streakTextView.text = "🔥 Your Streak: $streak Day(s)"
                }
                .addOnFailureListener {
                    streakTextView.text = "Could not load streak"
                    Toast.makeText(this, "Error loading streak", Toast.LENGTH_SHORT).show()
                }
        } else {
            streakTextView.text = "User not signed in"
        }
    }
}
