import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var chooseImgBtn: Button
    private lateinit var uploadImgBtn: Button
    private lateinit var retrieveImgBtn: Button
    private lateinit var imageView: ImageView
    private var fileUri: Uri? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chooseImgBtn = findViewById(R.id.choose_image)
        uploadImgBtn = findViewById(R.id.upload_image)
        retrieveImgBtn = findViewById(R.id.retrieve_image)
        imageView = findViewById(R.id.image_view)

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                fileUri = it
                imageView.setImageURI(it)
            }
        }

        chooseImgBtn.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        uploadImgBtn.setOnClickListener {
            if (fileUri != null) {
                uploadImage()
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
        }

        retrieveImgBtn.setOnClickListener {
            retrieveImage("your_image_filename.jpg") // Replace with actual filename
        }
    }

    private fun uploadImage() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading Image...")
        progressDialog.setMessage("Processing...")
        progressDialog.show()

        val fileName = UUID.randomUUID().toString() + ".jpg"
        val ref = FirebaseStorage.getInstance().reference.child("images/$fileName")

        ref.putFile(fileUri!!)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Uploaded successfully", Toast.LENGTH_SHORT).show()

                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("DownloadURL", "URL: $uri")
                    // Optionally store the URL in Firestore or Realtime DB here
                }
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun retrieveImage(filename: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Retrieving Image...")
        progressDialog.setMessage("Processing...")
        progressDialog.show()

        val imageRef: StorageReference = FirebaseStorage.getInstance().reference.child("images/$filename")

        imageRef.downloadUrl
            .addOnSuccessListener { uri: Uri ->
                Glide.with(this)
                    .load(uri)
                    .into(imageView)

                progressDialog.dismiss()
                Toast.makeText(this, "Image retrieved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
