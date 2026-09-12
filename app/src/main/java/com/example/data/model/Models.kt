package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apartments")
data class ApartmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val district: String, // Ваке, Сабуртало, Вера, Мтацминда, Дидубе, Чугурети, Исани
    val address: String,
    val priceUsd: Int,
    val rooms: Int,
    val areaSqM: Int,
    val floor: String,
    val hasFurniture: Boolean,
    val petsAllowed: Boolean,
    val minPeriod: String, // "На месяц", "От месяца до года", "От года"
    val maxTenants: Int,
    val landlordName: String,
    val landlordPhone: String,
    val description: String,
    val features: String, // Comma-separated list of amenities
    val imageDrawableName: String,
    val lat: Double,
    val lng: Double,
    val isLiked: Boolean = false,
    val isDisliked: Boolean = false,
    val likedTimestamp: Long = 0L
) {
    val featuresList: List<String>
        get() = features.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val isRegistered: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val rentalPeriod: String = "От месяца до года", // "На месяц", "От месяца до года", "От года"
    val tenantsCount: Int = 1,
    val hasPets: Boolean = false,
    val petType: String = "Нет животных",
    val preferredDistrict: String = "Все районы",
    val minBudget: Int = 300,
    val maxBudget: Int = 2000,
    val hasFurnitureFilter: Boolean? = null, // null: any, true: with furniture, false: without
    val telegramUsername: String = "",
    val telegramChatId: String = "",
    val telegramBotToken: String = "",
    val notificationsEnabled: Boolean = true
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val apartmentId: Long,
    val sender: String, // "BOT", "OWNER", "USER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewingProposal: Boolean = false,
    val isViewingConfirmed: Boolean = false,
    val viewingDate: String = ""
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val apartmentId: Long? = null,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class FilterState(
    val minBudget: Int = 300,
    val maxBudget: Int = 3000,
    val district: String = "Все районы",
    val furnitureRequirement: FurnitureFilter = FurnitureFilter.ANY,
    val petFriendlyOnly: Boolean = false,
    val rentalPeriod: String = "Любой"
)

enum class FurnitureFilter(val label: String) {
    ANY("Любая"),
    WITH_FURNITURE("С мебелью"),
    WITHOUT_FURNITURE("Без мебели")
}
