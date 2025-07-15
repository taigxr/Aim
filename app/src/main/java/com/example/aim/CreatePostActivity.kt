package com.example.aim

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.DayOfWeek

class CreatePostActivity : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var uploadPostButton: Button
    private lateinit var captionEditText: EditText
    private var imageUri: Uri? = null

    private val storage = Firebase.storage
    private val db = Firebase.firestore
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
        imagePreview.setImageURI(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (userId.isEmpty()) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        imagePreview = findViewById(R.id.imagePreview)
        selectImageButton = findViewById(R.id.selectImageButton)
        uploadPostButton = findViewById(R.id.uploadPostButton)
        captionEditText = findViewById(R.id.captionEditText)

        selectImageButton.setOnClickListener {
            imagePicker.launch("image/*")
        }

        uploadPostButton.setOnClickListener {
            if (imageUri == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val caption = captionEditText.text.toString()

            val timestamp = System.currentTimeMillis()
            val imageRef = storage.reference.child("posts/$userId/$timestamp.jpg")

            // Upload the image file
            imageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    // After upload, get the download URL
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Save post metadata to Firestore with caption and timestamp
                        savePostMetadataToFirestore(userId, uri.toString(), caption,
                            onSuccess = {
                                Toast.makeText(this, "Post uploaded!", Toast.LENGTH_SHORT).show()
                                handleWorkoutStreak(LocalDate.now())
                                finish()
                            },
                            onFailure = { e ->
                                Toast.makeText(this, "Failed to save post: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Download URL error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun savePostMetadataToFirestore(
        userId: String,
        imageUrl: String,
        caption: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val post = hashMapOf(
            "userId" to userId,
            "imageUrl" to imageUrl,
            "caption" to caption,
            "timestamp" to Timestamp.now()
        )

        db.collection("posts")
            .add(post)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun handleWorkoutStreak(photoUploadDate: LocalDate) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.get().addOnSuccessListener { doc ->
            val workoutDays = doc.get("workoutDays") as? List<String> ?: return@addOnSuccessListener
            val streak = doc.getLong("streak") ?: 0
            val lastWorkoutDateStr = doc.getString("lastWorkoutDate")
            val lastWorkoutDate = lastWorkoutDateStr?.let { LocalDate.parse(it) }

            val today = photoUploadDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() } // e.g. "Monday"

            if (workoutDays.contains(today)) {
                if (lastWorkoutDate != null && lastWorkoutDate == photoUploadDate.minusDays(1)) {
                    // Continue streak
                    userRef.update(
                        mapOf(
                            "streak" to streak + 1,
                            "lastWorkoutDate" to photoUploadDate.toString()
                        )
                    )
                } else if (lastWorkoutDate != photoUploadDate) {
                    // New streak or reset
                    userRef.update(
                        mapOf(
                            "streak" to 1,
                            "lastWorkoutDate" to photoUploadDate.toString()
                        )
                    )
                }
            }
        }
    }

}


