package com.example.aim

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FindFriendsActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: Button
    private lateinit var resultText: TextView
    private lateinit var sendRequestButton: Button
    private lateinit var incomingRequestsContainer: LinearLayout
    private lateinit var friendsContainer: LinearLayout


    private var foundUserId: String? = null
    private val db = Firebase.firestore
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)
        incomingRequestsContainer = findViewById(R.id.incoming_requests_container)
        loadIncomingFriendRequests()

        friendsContainer = findViewById(R.id.friends_container)
        loadFriends()



        // Bind views
        searchInput = findViewById(R.id.search_input)
        searchButton = findViewById(R.id.search_button)
        resultText = findViewById(R.id.result_text)
        sendRequestButton = findViewById(R.id.send_request_button)

        sendRequestButton.visibility = Button.GONE

        searchButton.setOnClickListener {
            val email = searchInput.text.toString().trim()
            if (email.isNotEmpty()) {
                searchUserByEmail(email)
            } else {
                Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            }
        }

        sendRequestButton.setOnClickListener {
            foundUserId?.let { targetUserId ->
                sendFriendRequest(targetUserId)
            }
        }
    }

    private fun searchUserByEmail(email: String) {
        db.collection("users")
            .whereEqualTo("profile.email", email)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val userDoc = result.documents[0]
                    foundUserId = userDoc.id
                    val username = userDoc.get("profile.username") as? String ?: "User"
                    resultText.text = "User found: $username"
                    sendRequestButton.visibility = Button.VISIBLE
                } else {
                    foundUserId = null
                    resultText.text = "No user found with that email"
                    sendRequestButton.visibility = Button.GONE
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@FindFriendsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadFriends() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Firebase.firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                val friends = doc.get("friends") as? List<String> ?: emptyList()

                friendsContainer.removeAllViews()

                if (friends.isEmpty()) {
                    val none = TextView(this).apply {
                        text = "You have no friends yet."
                    }
                    friendsContainer.addView(none)
                    return@addOnSuccessListener
                }

                for (friendId in friends) {
                    Firebase.firestore.collection("users").document(friendId)
                        .get()
                        .addOnSuccessListener { friendDoc ->
                            val username = (friendDoc["profile"] as? Map<*, *>)?.get("username") as? String ?: "Unknown"
                            val friendView = TextView(this)
                            friendView.text = username
                            friendView.textSize = 16f
                            friendsContainer.addView(friendView)
                        }
                }
            }
    }

    private fun loadIncomingFriendRequests() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Firebase.firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { doc ->
                val incoming = doc.get("incomingRequests") as? List<String> ?: emptyList()

                incomingRequestsContainer.removeAllViews()

                if (incoming.isEmpty()) {
                    val noRequests = TextView(this).apply {
                        text = "No incoming requests"
                    }
                    incomingRequestsContainer.addView(noRequests)
                    return@addOnSuccessListener
                }

                for (senderId in incoming) {
                    Firebase.firestore.collection("users").document(senderId)
                        .get()
                        .addOnSuccessListener { senderDoc ->
                            val username = (senderDoc["profile"] as? Map<*, *>)?.get("username") as? String ?: "Unknown"
                            val entry = layoutInflater.inflate(R.layout.item_friend_request, incomingRequestsContainer, false)

                            val nameText = entry.findViewById<TextView>(R.id.username_text)
                            val acceptButton = entry.findViewById<Button>(R.id.accept_button)

                            nameText.text = username
                            acceptButton.setOnClickListener {
                                acceptFriendRequest(senderId)
                            }

                            incomingRequestsContainer.addView(entry)
                        }
                }
            }
    }

    private fun sendFriendRequest(toUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore

        if (toUserId == currentUserId) {
            Toast.makeText(this, "You can't add yourself!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserRef = db.collection("users").document(currentUserId)

        // Step 1: Get your own friends list
        currentUserRef.get().addOnSuccessListener { doc ->
            val friends = doc.get("friends") as? List<String> ?: emptyList()

            // Step 2: Check if user is already a friend
            if (friends.contains(toUserId)) {
                Toast.makeText(this, "You're already friends!", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }

            // Step 3: Continue sending the request
            val toUserRef = db.collection("users").document(toUserId)

            // Add to sender's outgoingRequests
            currentUserRef.update("outgoingRequests", FieldValue.arrayUnion(toUserId))
                .addOnFailureListener {
                    currentUserRef.set(mapOf("outgoingRequests" to listOf(toUserId)), SetOptions.merge())
                }

            // Add to receiver's incomingRequests
            toUserRef.update("incomingRequests", FieldValue.arrayUnion(currentUserId))
                .addOnFailureListener {
                    toUserRef.set(mapOf("incomingRequests" to listOf(currentUserId)), SetOptions.merge())
                }

            Toast.makeText(this, "Friend request sent!", Toast.LENGTH_SHORT).show()
            sendRequestButton.visibility = Button.GONE
        }
    }


    fun acceptFriendRequest(senderUID: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = Firebase.firestore

        val currentUserRef = db.collection("users").document(currentUserId)
        val senderUserRef = db.collection("users").document(senderUID)

        // Remove senderUID from currentUser's incomingRequests
        currentUserRef.update("incomingRequests", FieldValue.arrayRemove(senderUID))

        // Remove currentUserId from sender's outgoingRequests
        senderUserRef.update("outgoingRequests", FieldValue.arrayRemove(currentUserId))

        // Add each other as friends
        currentUserRef.update("friends", FieldValue.arrayUnion(senderUID))
        senderUserRef.update("friends", FieldValue.arrayUnion(currentUserId))
            .addOnSuccessListener {
                Toast.makeText(this, "Friend request accepted!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error accepting request: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
