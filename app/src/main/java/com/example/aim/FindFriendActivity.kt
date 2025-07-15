package com.example.aim

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.aim.databinding.ActivityFindFriendsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FindFriendsActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var incomingRequestsContainer: LinearLayout
    private lateinit var friendsContainer: LinearLayout
    private val db = Firebase.firestore
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)

        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        incomingRequestsContainer = findViewById(R.id.incoming_requests_container)
        friendsContainer = findViewById(R.id.friends_container)

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchForUser(query)
            }
        }

        listenToIncomingFriendRequests()
        listenToFriends()
    }

    private fun searchForUser(username: String) {
        db.collection("users")
            .whereEqualTo("profile.username", username)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val userDoc = result.documents[0]
                    val userId = userDoc.id

                    if (userId == currentUserId) {
                        Toast.makeText(this, "That's you!", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    db.collection("users").document(currentUserId).get()
                        .addOnSuccessListener { myDoc ->
                            val friends = myDoc.get("friends") as? List<String> ?: emptyList()
                            val outgoing = myDoc.get("outgoingRequests") as? List<String> ?: emptyList()

                            if (friends.contains(userId)) {
                                Toast.makeText(this, "Already your friend", Toast.LENGTH_SHORT).show()
                            } else if (outgoing.contains(userId)) {
                                Toast.makeText(this, "Request already sent", Toast.LENGTH_SHORT).show()
                            } else {
                                sendFriendRequest(userId)
                            }
                        }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendFriendRequest(toUserId: String) {
        val currentUserRef = db.collection("users").document(currentUserId)
        val toUserRef = db.collection("users").document(toUserId)

        currentUserRef.update("outgoingRequests", FieldValue.arrayUnion(toUserId))
        toUserRef.update("incomingRequests", FieldValue.arrayUnion(currentUserId))
            .addOnSuccessListener {
                Toast.makeText(this, "Request sent!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun acceptFriendRequest(senderUID: String) {
        val currentUserRef = db.collection("users").document(currentUserId)
        val senderRef = db.collection("users").document(senderUID)

        currentUserRef.update("incomingRequests", FieldValue.arrayRemove(senderUID))
        senderRef.update("outgoingRequests", FieldValue.arrayRemove(currentUserId))

        currentUserRef.update("friends", FieldValue.arrayUnion(senderUID))
        senderRef.update("friends", FieldValue.arrayUnion(currentUserId))
            .addOnSuccessListener {
                Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenToIncomingFriendRequests() {
        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val incoming = snapshot.get("incomingRequests") as? List<String> ?: emptyList()
                    incomingRequestsContainer.removeAllViews()

                    for (senderId in incoming) {
                        db.collection("users").document(senderId).get()
                            .addOnSuccessListener { senderDoc ->
                                val username = (senderDoc["profile"] as? Map<*, *>)?.get("username") as? String ?: "Unknown"
                                val view = layoutInflater.inflate(R.layout.item_friend_request, incomingRequestsContainer, false)
                                view.findViewById<TextView>(R.id.username_text).text = username
                                view.findViewById<Button>(R.id.accept_button).setOnClickListener {
                                    acceptFriendRequest(senderId)
                                }
                                incomingRequestsContainer.addView(view)
                            }
                    }
                }
            }
    }

    private fun listenToFriends() {
        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null && snapshot.exists()) {
                    val friends = snapshot.get("friends") as? List<String> ?: emptyList()
                    friendsContainer.removeAllViews()

                    for (friendId in friends) {
                        db.collection("users").document(friendId).get()
                            .addOnSuccessListener { friendDoc ->
                                val username = (friendDoc["profile"] as? Map<*, *>)?.get("username") as? String ?: "Friend"
                                val streak = (friendDoc.getLong("workoutStreak") ?: 0).toInt()

                                val view = layoutInflater.inflate(R.layout.item_friend, friendsContainer, false)
                                view.findViewById<TextView>(R.id.friend_username_text).text = username
                                view.findViewById<TextView>(R.id.streak).text = "Day streak: $streak"

                                friendsContainer.addView(view)
                            }
                    }
                }
            }
    }
}
