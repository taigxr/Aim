package com.example.aim

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class WorkoutDaysActivity : AppCompatActivity() {

    private lateinit var checkboxes: List<CheckBox>
    private lateinit var saveButton: Button
    private lateinit var testPhotoButton: Button
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_days)

        checkboxes = listOf(
            findViewById(R.id.checkbox_monday),
            findViewById(R.id.checkbox_tuesday),
            findViewById(R.id.checkbox_wednesday),
            findViewById(R.id.checkbox_thursday),
            findViewById(R.id.checkbox_friday),
            findViewById(R.id.checkbox_saturday),
            findViewById(R.id.checkbox_sunday)
        )

        saveButton = findViewById(R.id.btn_save_days)
        testPhotoButton = findViewById(R.id.btn_test_photo)

        saveButton.setOnClickListener { saveWorkoutDays() }
        testPhotoButton.setOnClickListener { simulatePhotoUpload() }

        loadSavedWorkoutDays() // 🧠 Load previously saved days when opening the screen
    }

    private fun saveWorkoutDays() {
        if (userId == null) return

        val selectedDays = checkboxes
            .filter { it.isChecked }
            .map { it.text.toString() }

        db.collection("users").document(userId)
            .update("plannedWorkoutDays", selectedDays)
            .addOnSuccessListener {
                Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save days", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSavedWorkoutDays() {
        if (userId == null) return

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                val savedDays = doc.get("plannedWorkoutDays") as? List<*> ?: return@addOnSuccessListener
                checkboxes.forEach { checkbox ->
                    checkbox.isChecked = savedDays.contains(checkbox.text.toString())
                }
            }
    }

    private fun simulatePhotoUpload() {
        if (userId == null) return

        val today = LocalDate.now()
        val todayName = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)

        val userDoc = db.collection("users").document(userId)
        userDoc.get().addOnSuccessListener { doc ->
            val plannedDays = doc.get("plannedWorkoutDays") as? List<*> ?: return@addOnSuccessListener
            val lastWorkoutDateStr = doc.getString("lastWorkoutDate")
            var currentStreak = (doc.getLong("workoutStreak") ?: 0L).toInt()

            val hasWorkoutToday = plannedDays.contains(todayName)
            val lastWorkoutDate = lastWorkoutDateStr?.let { LocalDate.parse(it) }
            val yesterday = today.minusDays(1)

            // 🛑 Already logged today
            if (lastWorkoutDate == today) {
                Toast.makeText(this, "You already worked out today!", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // 🧯 Broke streak if missed a planned workout yesterday
            val brokeStreak = lastWorkoutDate != null &&
                    yesterday.isAfter(lastWorkoutDate) &&
                    plannedDays.contains(
                        yesterday.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    )

            if (brokeStreak) {
                currentStreak = 0
            }

            if (hasWorkoutToday) {
                currentStreak += 1
                userDoc.update(
                    mapOf(
                        "workoutStreak" to currentStreak,
                        "lastWorkoutDate" to today.toString()
                    )
                ).addOnSuccessListener {
                    Toast.makeText(this, "New streak: $currentStreak!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Not a planned workout day!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
