package com.example.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.data.local.RentchDao
import com.example.data.model.ApartmentEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FilterState
import com.example.data.model.FurnitureFilter
import com.example.data.model.NotificationEntity
import com.example.data.model.UserProfileEntity
import com.example.data.telegram.TelegramResult
import com.example.data.telegram.TelegramService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RentchRepository(
    private val dao: RentchDao,
    private val telegramService: TelegramService,
    private val context: Context
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    init {
        createNotificationChannel()
        repositoryScope.launch {
            initDataIfEmpty()
        }
    }

    private suspend fun initDataIfEmpty() {
        if (dao.getApartmentsCount() == 0) {
            dao.insertApartments(InitialData.getInitialApartments())
        }
        if (dao.getUserProfileSync() == null) {
            dao.saveUserProfile(UserProfileEntity())
        }
    }

    // Unswiped apartments filtered by user questionnaire and manual filters
    fun getFilteredApartments(filter: FilterState): Flow<List<ApartmentEntity>> {
        return combine(
            dao.getUnswipedApartments(),
            dao.getUserProfile()
        ) { apartments, profile ->
            apartments.filter { apt ->
                // Price filter
                val matchesPrice = apt.priceUsd in filter.minBudget..filter.maxBudget

                // District filter: manual filter takes precedence; if "Все районы", check profile preferredDistrict
                val targetDistrict = if (filter.district != "Все районы") {
                    filter.district
                } else if (profile?.preferredDistrict != null && profile.preferredDistrict != "Все районы") {
                    profile.preferredDistrict
                } else {
                    "Все районы"
                }
                val matchesDistrict = targetDistrict == "Все районы" || apt.district.equals(targetDistrict, ignoreCase = true)

                // Furniture filter
                val matchesFurniture = when (filter.furnitureRequirement) {
                    FurnitureFilter.ANY -> true
                    FurnitureFilter.WITH_FURNITURE -> apt.hasFurniture
                    FurnitureFilter.WITHOUT_FURNITURE -> !apt.hasFurniture
                }

                // Pets filter
                val matchesPets = if (filter.petFriendlyOnly || (profile?.hasPets == true)) {
                    apt.petsAllowed
                } else {
                    true
                }

                // Rental period filter
                val targetPeriod = if (filter.rentalPeriod != "Любой") {
                    filter.rentalPeriod
                } else if (profile?.rentalPeriod != null && profile.isRegistered) {
                    profile.rentalPeriod
                } else {
                    "Любой"
                }

                val matchesPeriod = if (targetPeriod == "Любой") {
                    true
                } else {
                    apt.minPeriod == targetPeriod || apt.minPeriod == "На месяц"
                }

                // Tenants capacity
                val matchesTenants = if (profile != null && profile.isRegistered) {
                    apt.maxTenants >= profile.tenantsCount
                } else {
                    true
                }

                matchesPrice && matchesDistrict && matchesFurniture && matchesPets && matchesPeriod && matchesTenants
            }
        }
    }

    fun getAllApartments(): Flow<List<ApartmentEntity>> = dao.getAllApartments()

    fun getLikedApartments(): Flow<List<ApartmentEntity>> = dao.getLikedApartments()

    fun getApartmentById(id: Long): Flow<ApartmentEntity?> = dao.getApartmentById(id)

    fun getUserProfile(): Flow<UserProfileEntity?> = dao.getUserProfile()

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        dao.saveUserProfile(profile)
    }

    // Swipe Right (Like / Rentch match)
    suspend fun swipeRight(apartment: ApartmentEntity) = withContext(Dispatchers.IO) {
        dao.setLiked(apartment.id, isLiked = true)

        // Generate automated initial bot message for the chat if not already started
        val messageCount = dao.getMessageCountForApartment(apartment.id)
        if (messageCount == 0) {
            val botWelcome = ChatMessageEntity(
                apartmentId = apartment.id,
                sender = "BOT",
                text = "Здравствуйте! Вы выбрали квартиру «${apartment.title}» в районе ${apartment.district} ($${apartment.priceUsd}/мес).\n\nЯ виртуальный консьерж Rentch. Хотите забронировать удобное время для просмотра квартиры с собственником (${apartment.landlordName})?",
                timestamp = System.currentTimeMillis()
            )
            dao.insertChatMessage(botWelcome)

            val proposalMsg = ChatMessageEntity(
                apartmentId = apartment.id,
                sender = "BOT",
                text = "Предложение: Просмотр квартиры с собственником",
                timestamp = System.currentTimeMillis() + 10,
                isViewingProposal = true,
                isViewingConfirmed = false,
                viewingDate = "Завтра, 15:00"
            )
            dao.insertChatMessage(proposalMsg)
        }

        // Send local notification
        val notificationTitle = "Rentch! У вас новое совпадение"
        val notificationText = "Квартира в ${apartment.district} за $${apartment.priceUsd}. Собственник ${apartment.landlordName} ждет в чате!"
        dao.insertNotification(
            NotificationEntity(
                apartmentId = apartment.id,
                title = notificationTitle,
                message = notificationText,
                timestamp = System.currentTimeMillis()
            )
        )
        showSystemNotification(notificationTitle, notificationText)

        // Telegram alert if configured
        val profile = dao.getUserProfileSync()
        if (profile != null && profile.telegramBotToken.isNotBlank() && profile.telegramChatId.isNotBlank()) {
            val tgText = """
                🎉 <b>Rentch: Новое совпадение!</b>
                🏠 <b>${apartment.title}</b>
                📍 Район: ${apartment.district}, ${apartment.address}
                💵 Цена: $${apartment.priceUsd}/мес
                👤 Собственник: ${apartment.landlordName} (${apartment.landlordPhone})
                🤖 Бот уже начал диалог для согласования просмотра!
            """.trimIndent()
            telegramService.sendMessage(profile.telegramBotToken, profile.telegramChatId, tgText)
        }
    }

    // Swipe Left (Pass)
    suspend fun swipeLeft(apartmentId: Long) = withContext(Dispatchers.IO) {
        dao.setDisliked(apartmentId, isDisliked = true)
    }

    // Reset Swipes for testing
    suspend fun resetAllSwipes() = withContext(Dispatchers.IO) {
        dao.resetAllSwipes()
    }

    // Chat operations
    fun getMessagesForApartment(apartmentId: Long): Flow<List<ChatMessageEntity>> {
        return dao.getMessagesForApartment(apartmentId)
    }

    suspend fun sendUserMessage(apartmentId: Long, text: String) = withContext(Dispatchers.IO) {
        dao.insertChatMessage(
            ChatMessageEntity(
                apartmentId = apartmentId,
                sender = "USER",
                text = text,
                timestamp = System.currentTimeMillis()
            )
        )

        // Landlord auto-reply simulation
        val apt = dao.getApartmentByIdSync(apartmentId)
        val profile = dao.getUserProfileSync()
        val userName = profile?.name?.ifBlank { "Алексей" } ?: "Гость"

        val replyText = when {
            text.contains("коммунал", ignoreCase = true) -> "Коммунальные услуги оплачиваются отдельно по счетчикам, в среднем около $50-70 в месяц. Отопление центральное."
            text.contains("залог", ignoreCase = true) || text.contains("депозит", ignoreCase = true) -> "Оплата за первый и последний месяц в качестве залога. Залог возвращается при выезде."
            text.contains("договор", ignoreCase = true) -> "Да, конечно, заключаем официальный двуязычный договор аренды (грузинский/русский/английский) с регистрацией."
            text.contains("интернет", ignoreCase = true) || text.contains("wifi", ignoreCase = true) -> "Подключен оптоволоконный Magti 100 Мбит/с, роутер уже настроен."
            text.contains("метро", ignoreCase = true) -> "До ближайшей станции метро пешком 5-7 минут не спеша."
            else -> "Спасибо за сообщение, $userName! Я на связи. Вы сможете посмотреть квартиру лично в назначенное время или напишите, если хотите перенести."
        }

        dao.insertChatMessage(
            ChatMessageEntity(
                apartmentId = apartmentId,
                sender = "OWNER",
                text = replyText,
                timestamp = System.currentTimeMillis() + 800
            )
        )
    }

    suspend fun confirmViewingRequest(
        messageId: Long,
        apartmentId: Long,
        userProfile: UserProfileEntity
    ) = withContext(Dispatchers.IO) {
        dao.confirmViewingMessage(messageId)

        val apt = dao.getApartmentByIdSync(apartmentId) ?: return@withContext

        // Bot message confirming booking
        val botConfirmation = ChatMessageEntity(
            apartmentId = apartmentId,
            sender = "BOT",
            text = "✅ Заявка на просмотр успешно подтверждена!\n\n" +
                    "👤 Арендатор: ${userProfile.name.ifBlank { "Гость" }}\n" +
                    "📞 Телефон: ${userProfile.phone.ifBlank { "Не указан" }}\n" +
                    "⏱ Срок: ${userProfile.rentalPeriod}\n" +
                    "👥 Проживающих: ${userProfile.tenantsCount} чел.\n" +
                    "🐾 Животные: ${if (userProfile.hasPets) userProfile.petType else "Нет"}\n" +
                    "📍 Желаемый район: ${userProfile.preferredDistrict}\n\n" +
                    "Собственник ${apt.landlordName} уведомлен о вашей записи!",
            timestamp = System.currentTimeMillis()
        )
        dao.insertChatMessage(botConfirmation)

        // Landlord welcoming message
        val ownerConfirmation = ChatMessageEntity(
            apartmentId = apartmentId,
            sender = "OWNER",
            text = "Здравствуйте, ${userProfile.name.ifBlank { "будущий жилец" }}! Заявка принята. " +
                    "Буду рад встретить вас завтра в 15:00 по адресу ${apt.address}. " +
                    "Мой телефон: ${apt.landlordPhone}. До встречи!",
            timestamp = System.currentTimeMillis() + 600
        )
        dao.insertChatMessage(ownerConfirmation)

        // Telegram alert for viewing
        if (userProfile.telegramBotToken.isNotBlank() && userProfile.telegramChatId.isNotBlank()) {
            val tgViewingText = """
                📅 <b>Rentch: Просмотр подтвержден!</b>
                🏠 <b>${apt.title}</b>
                📍 Адрес: ${apt.address}
                💵 Цена: $${apt.priceUsd}/мес
                👤 Собственник: ${apt.landlordName} (${apt.landlordPhone})
                ⏰ Время: Завтра в 15:00
                ✅ Ваши ответы на опросник переданы владельцу.
            """.trimIndent()
            telegramService.sendMessage(userProfile.telegramBotToken, userProfile.telegramChatId, tgViewingText)
        }
    }

    // Real-time notification simulation
    suspend fun simulateIncomingApartmentAlert(): ApartmentEntity = withContext(Dispatchers.IO) {
        val sampleNew = ApartmentEntity(
            title = "Стильная 2-к студия на проспекте Руставели",
            district = "Мтацминда",
            address = "пр. Шота Руставели, 22, Тбилиси",
            priceUsd = 880,
            rooms = 2,
            areaSqM = 58,
            floor = "4/7 эт.",
            hasFurniture = true,
            petsAllowed = true,
            minPeriod = "От месяца до года",
            maxTenants = 2,
            landlordName = "Лаша Гурули",
            landlordPhone = "+995 599 010 492",
            description = "Горячее новое предложение в самом центре Тбилиси! Балкон выходит на тихий сквер. Вся мебель и техника новые. Отличная транспортная доступность.",
            features = "Центр, Свежий ремонт, Посудомойка, Кондиционер, С питомцами, Тихий сквер",
            imageDrawableName = "img_apt_mtatsminda",
            lat = 41.7000,
            lng = 44.7950
        )
        val id = dao.insertApartment(sampleNew)
        val created = sampleNew.copy(id = id)

        val title = "🔥 Новый объект в Тбилиси: ${created.district}"
        val text = "${created.title} всего за $${created.priceUsd}/мес! Подходит по вашим критериям."

        dao.insertNotification(
            NotificationEntity(
                apartmentId = id,
                title = title,
                message = text,
                timestamp = System.currentTimeMillis()
            )
        )
        showSystemNotification(title, text)

        val profile = dao.getUserProfileSync()
        if (profile != null && profile.telegramBotToken.isNotBlank() && profile.telegramChatId.isNotBlank()) {
            val tgAlert = """
                🔥 <b>Rentch: Новый подходящий объект!</b>
                🏠 <b>${created.title}</b>
                📍 Район: ${created.district}, ${created.address}
                💵 Цена: $${created.priceUsd}/мес
                🐾 С питомцами: Да
                🛋 С мебелью: Да
                Откройте приложение Rentch, чтобы свайпнуть!
            """.trimIndent()
            telegramService.sendMessage(profile.telegramBotToken, profile.telegramChatId, tgAlert)
        }

        created
    }

    fun getAllNotifications(): Flow<List<NotificationEntity>> = dao.getAllNotifications()

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        dao.markAllNotificationsAsRead()
    }

    suspend fun testTelegramNotification(token: String, chatId: String): TelegramResult {
        val message = """
            👋 <b>Rentch Bot уведомления подключены!</b>
            Вы будете мгновенно получать:
            • Новые подходящие квартиры по вашим критериям в Тбилиси
            • Уведомления о совпадениях (Rentch)
            • Записи и напоминания о просмотрах с собственниками
        """.trimIndent()
        return telegramService.sendMessage(token, chatId, message)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Rentch Уведомления о недвижимости",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Мгновенные оповещения о совпадениях, просмотрах и новых квартирах в Тбилиси"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showSystemNotification(title: String, message: String) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            manager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
        } catch (e: Exception) {
            // Ignored if permission is pending or restricted
        }
    }

    companion object {
        private const val CHANNEL_ID = "rentch_alerts_channel"
    }
}
