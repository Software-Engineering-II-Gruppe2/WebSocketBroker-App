package at.aau.serg.websocketbrokerdemo.data

import android.util.Log
import at.aau.serg.websocketbrokerdemo.logic.UserProfileProvider
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object FirestoreManager : UserProfileProvider{
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun saveUserProfile(userId: String, profile: PlayerProfile) {
        try {
            usersCollection.document(userId).set(profile).await()
            Log.d("FirestoreManager", "UserProfile saved successfully for current userId")
        } catch (e: FirebaseException) {
            Log.e("FirestoreManager", "Error saving UserProfile for this userId ", e)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "An unexpected error occurred while saving UserProfile for actual userId", e)
        }
    }

    override suspend fun getUserProfile(userId: String): PlayerProfile? {
        return try {
            val document = usersCollection.document(userId).get().await()
            val profile = document.toObject(PlayerProfile::class.java)
            Log.d("FirestoreManager", "UserProfile retrieved successfully for your userId")
            profile
        } catch (e: FirebaseException) {
            Log.e("FirestoreManager", "Error retrieving UserProfile", e)
            null
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Unexpected error when retrieving profile", e)
            null
        }
    }

    suspend fun updateUserProfileName(userId: String, newName: String) {
        try {
            usersCollection.document(userId).update("name", newName).await()
            Log.d("FirestoreManager", "UserProfile name updated successfully")
        } catch (e: FirebaseException) {
            Log.e("FirestoreManager", "Error updating UserProfile name", e)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Unexpected error when updating profile", e)
        }
    }

    suspend fun getGameHistory(userId: String): List<GameData> {
        return try {
            usersCollection.document(userId)
                .collection("gameHistory")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(GameData::class.java)
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error getting game history", e)
            emptyList()
        }
    }

    suspend fun getLeaderboardEntries(leaderboardType: String): List<Map<String, Any>> {
        return try {
            db.collection("leaderboard_$leaderboardType")
                .orderBy("rank", Query.Direction.ASCENDING)
                .get()
                .await()
                .documents
                .map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error getting leaderboard entries", e)
            emptyList()
        }
    }

    fun listenToUserProfile(userId: String, onProfileChanged: (PlayerProfile?) -> Unit) {
        val docRef = FirebaseFirestore.getInstance().collection("users").document(userId)
        docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreManager", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(PlayerProfile::class.java)
                onProfileChanged(profile)
            } else {
                onProfileChanged(null)
            }
        }
    }
}
