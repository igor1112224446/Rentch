package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationAdd
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApartmentEntity
import com.example.data.model.FilterState
import com.example.data.model.FurnitureFilter
import com.example.ui.components.SwipeCard
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenMatch
import com.example.ui.theme.RedNope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeScreen(
    apartments: List<ApartmentEntity>,
    filterState: FilterState,
    onSwipeRight: (ApartmentEntity) -> Unit,
    onSwipeLeft: (ApartmentEntity) -> Unit,
    onShowDetails: (ApartmentEntity) -> Unit,
    onOpenFilters: () -> Unit,
    onResetSwipes: () -> Unit,
    onTriggerNewObjectAlert: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeFilterCount = (if (filterState.district != "Все районы") 1 else 0) +
            (if (filterState.furnitureRequirement != FurnitureFilter.ANY) 1 else 0) +
            (if (filterState.petFriendlyOnly) 1 else 0) +
            (if (filterState.minBudget > 300 || filterState.maxBudget < 3000) 1 else 0)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CoralPrimary,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Rentch",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = CoralPrimary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Тбилиси • Поиск квартир",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Simulate real-time notification
                    IconButton(
                        onClick = onTriggerNewObjectAlert,
                        modifier = Modifier.testTag("simulate_new_alert_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Новый объект",
                            tint = CoralPrimary
                        )
                    }

                    // Filter Button with Badge
                    IconButton(
                        onClick = onOpenFilters,
                        modifier = Modifier.testTag("open_filters_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge(containerColor = CoralPrimary) {
                                        Text("$activeFilterCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Фильтры"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Deck Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (apartments.isEmpty()) {
                    // Empty state
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .padding(24.dp)
                            .testTag("empty_apartments_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(CoralContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = CoralPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Все варианты просмотрены!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Сбросьте историю свайпов или расширьте фильтры поиска (бюджет, районы Тбилиси).",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = onResetSwipes,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CoralPrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("reset_swipes_button")
                            ) {
                                Text("Начать заново (сбросить свайпы)", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onOpenFilters,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Изменить фильтры")
                            }
                        }
                    }
                } else {
                    // Render Next Card underneath for depth
                    if (apartments.size > 1) {
                        val nextApt = apartments[1]
                        SwipeCard(
                            apartment = nextApt,
                            onSwipeRight = {},
                            onSwipeLeft = {},
                            onShowDetails = {},
                            modifier = Modifier
                                .fillMaxSize(0.95f)
                                .scale(0.95f)
                        )
                    }

                    // Top Card (Interactive)
                    val topApt = apartments.first()
                    SwipeCard(
                        apartment = topApt,
                        onSwipeRight = { onSwipeRight(topApt) },
                        onSwipeLeft = { onSwipeLeft(topApt) },
                        onShowDetails = { onShowDetails(topApt) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row (Nope, Details, Rentch Like)
            if (apartments.isNotEmpty()) {
                val currentApt = apartments.first()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Pass Button (Dislike)
                    FloatingActionButton(
                        onClick = { onSwipeLeft(currentApt) },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = RedNope,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(4.dp, CircleShape)
                            .testTag("pass_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Пропустить",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 2. Info / Details Button
                    FloatingActionButton(
                        onClick = { onShowDetails(currentApt) },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(2.dp, CircleShape)
                            .testTag("info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Подробнее",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 3. Rentch / Like Button
                    FloatingActionButton(
                        onClick = { onSwipeRight(currentApt) },
                        containerColor = CoralPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(62.dp)
                            .shadow(6.dp, CircleShape)
                            .testTag("like_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Rentch! Нравится",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
