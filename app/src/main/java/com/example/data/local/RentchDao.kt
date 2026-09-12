package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApartmentEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RentchDao {
    // Apartments
    @Query("SELECT * FROM apartments ORDER BY id ASC")
    fun getAllApartments(): Flow<List<ApartmentEntity>>

    @Query("SELECT * FROM apartments WHERE isLiked = 0 AND isDisliked = 0 ORDER BY id ASC")
    fun getUnswipedApartments(): Flow<List<ApartmentEntity>>

    @Query("SELECT * FROM apartments WHERE isLiked = 1 ORDER BY likedTimestamp DESC")
    fun getLikedApartments(): Flow<List<ApartmentEntity>>

    @Query("SELECT * FROM apartments WHERE id = :id LIMIT 1")
    fun getApartmentById(id: Long): Flow<ApartmentEntity?>

    @Query("SELECT * FROM apartments WHERE id = :id LIMIT 1")
    suspend fun getApartmentByIdSync(id: Long): ApartmentEntity?

    @Query("SELECT COUNT(*) FROM apartments")
    suspend fun getApartmentsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApartments(apartments: List<ApartmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApartment(apartment: ApartmentEntity): Long

    @Update
    suspend fun updateApartment(apartment: ApartmentEntity)

    @Query("UPDATE apartments SET isLiked = :isLiked, likedTimestamp = :timestamp WHERE id = :id")
    suspend fun setLiked(id: Long, isLiked: Boolean, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE apartments SET isDisliked = :isDisliked WHERE id = :id")
    suspend fun setDisliked(id: Long, isDisliked: Boolean)

    @Query("UPDATE apartments SET isLiked = 0, isDisliked = 0, likedTimestamp = 0")
    suspend fun resetAllSwipes()

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileSync(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE apartmentId = :apartmentId ORDER BY timestamp ASC")
    fun getMessagesForApartment(apartmentId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT COUNT(*) FROM chat_messages WHERE apartmentId = :apartmentId")
    suspend fun getMessageCountForApartment(apartmentId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Update
    suspend fun updateChatMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET isViewingConfirmed = 1 WHERE id = :messageId")
    suspend fun confirmViewingMessage(messageId: Long)

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllNotificationsAsRead()
}
