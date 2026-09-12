package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.RentchDatabase
import com.example.data.model.ApartmentEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FilterState
import com.example.data.model.FurnitureFilter
import com.example.data.model.NotificationEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.RentchRepository
import com.example.data.telegram.TelegramResult
import com.example.data.telegram.TelegramService
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RentchViewModel(application: Application) : AndroidViewModel(application) {
    private val database = RentchDatabase.getInstance(application)
    private val telegramService = TelegramService()
    val repository = RentchRepository(database.rentchDao(), telegramService, application)

    // Manual Filters
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Filtered unswiped cards for Tinder Swiping
    @OptIn(ExperimentalCoroutinesApi::class)
    val availableApartments: StateFlow<List<ApartmentEntity>> = _filterState
        .flatMapLatest { filter -> repository.getFilteredApartments(filter) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Liked apartments (Matches list)
    val likedApartments: StateFlow<List<ApartmentEntity>> = repository.getLikedApartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All apartments for Map display
    val allApartments: StateFlow<List<ApartmentEntity>> = repository.getAllApartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User profile with questionnaire responses
    val userProfile: StateFlow<UserProfileEntity?> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Notifications list
    val notifications: StateFlow<List<NotificationEntity>> = repository.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Rentch Match Dialog (Celebration modal)
    private val _rentchMatchApartment = MutableStateFlow<ApartmentEntity?>(null)
    val rentchMatchApartment: StateFlow<ApartmentEntity?> = _rentchMatchApartment.asStateFlow()

    // Active Chat state
    private val _currentChatApartment = MutableStateFlow<ApartmentEntity?>(null)
    val currentChatApartment: StateFlow<ApartmentEntity?> = _currentChatApartment.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentChatMessages: StateFlow<List<ChatMessageEntity>> = _currentChatApartment
        .flatMapLatest { apt ->
            if (apt != null) {
                repository.getMessagesForApartment(apt.id)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Questionnaire modal state
    private val _showQuestionnaire = MutableStateFlow(false)
    val showQuestionnaire: StateFlow<Boolean> = _showQuestionnaire.asStateFlow()

    private var pendingMessageId: Long? = null
    private var pendingApartmentId: Long? = null

    // Status snackbar / feedback
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    fun dismissFeedback() {
        _userFeedbackMessage.value = null
    }

    // Swipe Right (Rentch Match)
    fun onSwipeRight(apartment: ApartmentEntity) {
        viewModelScope.launch {
            repository.swipeRight(apartment)
            _rentchMatchApartment.value = apartment
        }
    }

    // Swipe Left (Pass)
    fun onSwipeLeft(apartment: ApartmentEntity) {
        viewModelScope.launch {
            repository.swipeLeft(apartment.id)
        }
    }

    fun dismissRentchMatch() {
        _rentchMatchApartment.value = null
    }

    fun openChatForApartment(apartment: ApartmentEntity) {
        _rentchMatchApartment.value = null
        _currentChatApartment.value = apartment
    }

    fun closeChat() {
        _currentChatApartment.value = null
    }

    // Viewing booking request clicked in Chat
    fun onConfirmViewingClicked(messageId: Long, apartmentId: Long) {
        val profile = userProfile.value
        if (profile == null || !profile.isRegistered) {
            // Need to register / answer questionnaire first!
            pendingMessageId = messageId
            pendingApartmentId = apartmentId
            _showQuestionnaire.value = true
        } else {
            // User already answered questionnaire, directly confirm
            viewModelScope.launch {
                repository.confirmViewingRequest(messageId, apartmentId, profile)
                _userFeedbackMessage.value = "Заявка на просмотр успешно отправлена собственнику!"
            }
        }
    }

    fun dismissQuestionnaire() {
        _showQuestionnaire.value = false
        pendingMessageId = null
        pendingApartmentId = null
    }

    fun submitQuestionnaire(
        name: String,
        phone: String,
        rentalPeriod: String,
        tenantsCount: Int,
        hasPets: Boolean,
        petType: String,
        preferredDistrict: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            val updated = current.copy(
                isRegistered = true,
                name = name.trim(),
                phone = phone.trim(),
                rentalPeriod = rentalPeriod,
                tenantsCount = tenantsCount,
                hasPets = hasPets,
                petType = petType,
                preferredDistrict = preferredDistrict
            )
            repository.saveUserProfile(updated)
            _showQuestionnaire.value = false

            // If triggered from chat viewing request, complete it now
            val msgId = pendingMessageId
            val aptId = pendingApartmentId
            if (msgId != null && aptId != null) {
                repository.confirmViewingRequest(msgId, aptId, updated)
                _userFeedbackMessage.value = "Вы успешно зарегистрированы и записаны на просмотр!"
            } else {
                _userFeedbackMessage.value = "Профиль сохранен! Квартиры отфильтрованы по вашим критериям."
            }
            pendingMessageId = null
            pendingApartmentId = null
        }
    }

    fun sendMessage(text: String) {
        val apt = _currentChatApartment.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendUserMessage(apt.id, text.trim())
        }
    }

    fun updateFilters(newFilter: FilterState) {
        _filterState.value = newFilter
    }

    fun updateTelegramSettings(botToken: String, chatId: String, username: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            val updated = current.copy(
                telegramBotToken = botToken.trim(),
                telegramChatId = chatId.trim(),
                telegramUsername = username.trim()
            )
            repository.saveUserProfile(updated)
            _userFeedbackMessage.value = "Настройки Telegram обновлены"
        }
    }

    fun testTelegram(botToken: String, chatId: String) {
        viewModelScope.launch {
            _userFeedbackMessage.value = "Отправка тестового оповещения в Telegram..."
            when (val res = repository.testTelegramNotification(botToken, chatId)) {
                is TelegramResult.Success -> _userFeedbackMessage.value = res.message
                is TelegramResult.Error -> _userFeedbackMessage.value = "Ошибка Telegram: ${res.errorMessage}"
            }
        }
    }

    fun triggerSimulatedNewApartment() {
        viewModelScope.launch {
            val created = repository.simulateIncomingApartmentAlert()
            _userFeedbackMessage.value = "🔥 Новая подходящая квартира в ${created.district} добавлена!"
        }
    }

    fun resetAllSwipes() {
        viewModelScope.launch {
            repository.resetAllSwipes()
            _userFeedbackMessage.value = "Все свайпы сброшены! Все квартиры снова доступны."
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }
}
