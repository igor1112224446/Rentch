package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ApartmentEntity
import com.example.ui.components.ApartmentDetailsDialog
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.QuestionnaireDialog
import com.example.ui.components.RentchMatchDialog
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ConversationsScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SwipeScreen
import com.example.ui.screens.WebScreen
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralPrimary
import com.example.ui.viewmodel.RentchViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    SWIPE("Поиск", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    MAP("Карта", Icons.Filled.Map, Icons.Outlined.Map),
    CHATS("Диалоги", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    WEB("Веб", Icons.Filled.Language, Icons.Outlined.Language),
    PROFILE("Профиль", Icons.Filled.Person, Icons.Outlined.PersonOutline)
}

@Composable
fun RentchApp(
    viewModel: RentchViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(MainTab.WEB) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Observables from ViewModel
    val availableApartments by viewModel.availableApartments.collectAsState()
    val likedApartments by viewModel.likedApartments.collectAsState()
    val allApartments by viewModel.allApartments.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val matchCelebrationApartment by viewModel.rentchMatchApartment.collectAsState()
    val currentChatApartment by viewModel.currentChatApartment.collectAsState()
    val currentChatMessages by viewModel.currentChatMessages.collectAsState()
    val showQuestionnaire by viewModel.showQuestionnaire.collectAsState()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsState()

    // UI dialog states
    var showFilterSheet by remember { mutableStateOf(false) }
    var selectedDetailsApartment by remember { mutableStateOf<ApartmentEntity?>(null) }
    var isManualEditingQuestionnaire by remember { mutableStateOf(false) }

    // Show feedback in Snackbar
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissFeedback()
        }
    }

    // Active Chat takes over full view if opened
    val chatApt = currentChatApartment
    if (chatApt != null) {
        ChatScreen(
            apartment = chatApt,
            messages = currentChatMessages,
            onBackClick = { viewModel.closeChat() },
            onSendMessage = { viewModel.sendMessage(it) },
            onConfirmViewing = { messageId, aptId ->
                viewModel.onConfirmViewingClicked(messageId, aptId)
            }
        )
    } else {
        val tabContent: @Composable () -> Unit = {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    MainTab.SWIPE -> {
                        SwipeScreen(
                            apartments = availableApartments,
                            filterState = filterState,
                            onSwipeRight = { viewModel.onSwipeRight(it) },
                            onSwipeLeft = { viewModel.onSwipeLeft(it) },
                            onShowDetails = { selectedDetailsApartment = it },
                            onOpenFilters = { showFilterSheet = true },
                            onResetSwipes = { viewModel.resetAllSwipes() },
                            onTriggerNewObjectAlert = { viewModel.triggerSimulatedNewApartment() }
                        )
                    }
                    MainTab.MAP -> {
                        MapScreen(
                            apartments = allApartments,
                            onSelectApartment = { selectedDetailsApartment = it },
                            onStartChat = { viewModel.openChatForApartment(it) },
                            onLikeApartment = { viewModel.onSwipeRight(it) }
                        )
                    }
                    MainTab.CHATS -> {
                        ConversationsScreen(
                            likedApartments = likedApartments,
                            onOpenChat = { viewModel.openChatForApartment(it) }
                        )
                    }
                    MainTab.WEB -> {
                        WebScreen()
                    }
                    MainTab.PROFILE -> {
                        ProfileScreen(
                            userProfile = userProfile,
                            notifications = notifications,
                            onEditQuestionnaire = { isManualEditingQuestionnaire = true },
                            onSaveTelegramSettings = { token, chatId, username ->
                                viewModel.updateTelegramSettings(token, chatId, username)
                            },
                            onTestTelegram = { token, chatId ->
                                viewModel.testTelegram(token, chatId)
                            },
                            onTriggerNewObject = { viewModel.triggerSimulatedNewApartment() }
                        )
                    }
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isExpanded = maxWidth >= 720.dp

            if (isExpanded) {
                // Canonical Desktop / Tablet Layout: Side NavigationRail + Content Pane
                Row(modifier = Modifier.fillMaxSize()) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = CoralPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("R", fontWeight = FontWeight.Black, color = CoralPrimary, fontSize = 20.sp)
                                    }
                                }
                                Text(
                                    text = "Rentch",
                                    fontWeight = FontWeight.Bold,
                                    color = CoralPrimary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        },
                        modifier = Modifier.testTag("desktop_nav_rail")
                    ) {
                        MainTab.values().forEach { tab ->
                            val isSelected = currentTab == tab
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { currentTab = tab },
                                icon = {
                                    if (tab == MainTab.CHATS && likedApartments.isNotEmpty()) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = CoralPrimary) {
                                                    Text("${likedApartments.size}")
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    }
                                },
                                label = { Text(tab.title) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = CoralPrimary,
                                    selectedTextColor = CoralPrimary,
                                    indicatorColor = CoralContainer
                                ),
                                modifier = Modifier.testTag("nav_rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }

                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        modifier = Modifier.weight(1f)
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            tabContent()
                        }
                    }
                }
            } else {
                // Compact Handheld Phone Layout: Bottom NavigationBar
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp,
                            modifier = Modifier.testTag("bottom_nav_bar")
                        ) {
                            MainTab.values().forEach { tab ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        if (tab == MainTab.CHATS && likedApartments.isNotEmpty()) {
                                            BadgedBox(
                                                badge = {
                                                    Badge(containerColor = CoralPrimary) {
                                                        Text("${likedApartments.size}")
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.title
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.title
                                            )
                                        }
                                    },
                                    label = { Text(tab.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = CoralPrimary,
                                        selectedTextColor = CoralPrimary,
                                        indicatorColor = CoralContainer
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        tabContent()
                    }
                }
            }
        }
    }

    // Modal 1: Rentch Match Celebration Dialog (When swiped right)
    matchCelebrationApartment?.let { apt ->
        RentchMatchDialog(
            apartment = apt,
            onStartChat = { viewModel.openChatForApartment(apt) },
            onDismiss = { viewModel.dismissRentchMatch() }
        )
    }

    // Modal 2: Questionnaire & Registration Dialog
    if (showQuestionnaire || isManualEditingQuestionnaire) {
        QuestionnaireDialog(
            initialProfile = userProfile,
            onDismiss = {
                viewModel.dismissQuestionnaire()
                isManualEditingQuestionnaire = false
            },
            onSubmit = { name, phone, period, tenants, hasPets, petType, district ->
                viewModel.submitQuestionnaire(
                    name = name,
                    phone = phone,
                    rentalPeriod = period,
                    tenantsCount = tenants,
                    hasPets = hasPets,
                    petType = petType,
                    preferredDistrict = district
                )
                isManualEditingQuestionnaire = false
            }
        )
    }

    // Modal 3: Search Filters Bottom Sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilter = filterState,
            onDismiss = { showFilterSheet = false },
            onApply = { newFilter ->
                viewModel.updateFilters(newFilter)
            }
        )
    }

    // Modal 4: Apartment Detailed Specs View
    selectedDetailsApartment?.let { apt ->
        ApartmentDetailsDialog(
            apartment = apt,
            onDismiss = { selectedDetailsApartment = null },
            onStartChat = {
                selectedDetailsApartment = null
                viewModel.openChatForApartment(apt)
            }
        )
    }
}
