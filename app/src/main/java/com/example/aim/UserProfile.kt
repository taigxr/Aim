package com.example.aim

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.Log
import com.example.aim.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UserProfile : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private lateinit var user: FirebaseUser
    private lateinit var uid: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()

        if (uid != null) {
            dbRef.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists())
                    {
                        val name = document.getString("displayName")
                        val account = document.getString("email")
                        val photoUrl = document.getString("photoUrl")

                        // Update views
                        binding.fullNameEditText.setText(name ?: "Anonymous Athlete")
                        binding.userAccountField.text = account ?: "@user"

                        if (!photoUrl.isNullOrEmpty())
                        {
                            Glide.with(this)
                                .load(photoUrl)
                                .placeholder(R.drawable.default_avatar)
                                .circleCrop()
                                .into(binding.profileImage)
                        }
                    }
                }
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

        }

        var isEditing = false

        binding.editNameButton.setOnClickListener {
            isEditing = !isEditing
            if (isEditing)
            {
                binding.fullNameEditText.isEnabled = true
                binding.fullNameEditText.requestFocus()
                binding.editNameButton.setImageResource(R.drawable.ic_save)
            }
            else
            {
                val newName = binding.fullNameEditText.text.toString()
                val user = FirebaseAuth.getInstance().currentUser
                val updates = userProfileChangeRequest {
                    displayName = newName
                }
                user?.updateProfile(updates)
                binding.fullNameEditText.isEnabled = false
                binding.editNameButton.setImageResource(R.drawable.ic_edit)
            }
        }
        binding.cameraIcon.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.buttonLogout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
    private fun uploadToStorageAndUpdateProfile(imageUri: Uri) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance()
            .reference.child("profile_images/$uid.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateProfileWithPhoto(downloadUri)
                    savePhotoToFirestore(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Upload failed", Toast.LENGTH_SHORT).show()
            }
    }
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            binding.profileImage.setImageURI(uri) // preview
            uploadToStorageAndUpdateProfile(uri)
        }
    }
    private fun updateProfileWithPhoto(photoUri: Uri) {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setPhotoUri(photoUri)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated in FirebaseAuth", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update FirebaseAuth", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun savePhotoToFirestore(url: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(uid)
            .update("photoUrl", url)
    }
}