package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApartmentEntity
import com.example.data.model.ChatMessageEntity
import com.example.ui.components.DrawableResolver
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenMatch
import com.example.ui.theme.GreenMatchContainer
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    apartment: ApartmentEntity,
    messages: List<ChatMessageEntity>,
    onBackClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onConfirmViewing: (messageId: Long, apartmentId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().imePadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val imageRes = DrawableResolver.getDrawableId(context, apartment.imageDrawableName)
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = apartment.title,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = apartment.landlordName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${apartment.district} • $${apartment.priceUsd}/мес",
                                fontSize = 12.sp,
                                color = CoralPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Chat Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Header security info
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🔒 Безопасная аренда Rentch. Диалог начат автоматически для бронирования просмотра.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        apartment = apartment,
                        onConfirmViewing = { onConfirmViewing(message.id, apartment.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Quick Question Chips
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickPromptChip(text = "Включены ли КУ?") { inputText = "Подскажите, пожалуйста, включены ли коммунальные услуги в стоимость?" }
                QuickPromptChip(text = "Какой залог?") { inputText = "Какой размер залога и порядок его возврата?" }
                QuickPromptChip(text = "Есть ли Wi-Fi?") { inputText = "Какой интернет проведен в квартире и какова скорость?" }
            }

            // Input Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Сообщение собственнику...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(CoralPrimary, CircleShape)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Отправить",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessageEntity,
    apartment: ApartmentEntity,
    onConfirmViewing: () -> Unit
) {
    val isUser = message.sender == "USER"
    val isBot = message.sender == "BOT"
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeStr = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender Label & Icon for BOT and OWNER
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(if (isBot) IndigoContainer else CoralContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBot) Icons.Default.SmartToy else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isBot) IndigoSecondary else CoralPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isBot) "Виртуальный консьерж Rentch" else apartment.landlordName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBot) IndigoSecondary else CoralPrimary
                )
            }
        }

        // Special Viewing Proposal Interactive Card
        if (message.isViewingProposal) {
            ViewingProposalCard(
                message = message,
                apartment = apartment,
                onConfirm = onConfirmViewing
            )
        } else {
            // Standard Bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = when {
                    isUser -> CoralPrimary
                    isBot -> IndigoContainer.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text = message.text,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = if (isUser) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewingProposalCard(
    message: ChatMessageEntity,
    apartment: ApartmentEntity,
    onConfirm: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .border(1.5.dp, if (message.isViewingConfirmed) GreenMatch else CoralPrimary, RoundedCornerShape(16.dp))
            .testTag("viewing_proposal_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (message.isViewingConfirmed) GreenMatchContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (message.isViewingConfirmed) GreenMatchContainer else CoralContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (message.isViewingConfirmed) Icons.Default.CheckCircle else Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = if (message.isViewingConfirmed) GreenMatch else CoralPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (message.isViewingConfirmed) "Просмотр назначен!" else "Предложение: Просмотр квартиры",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (message.isViewingConfirmed) GreenMatch else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Предложенное время: ${message.viewingDate.ifBlank { "Завтра, 15:00" }}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (message.isViewingConfirmed)
                    "Собственник ${apartment.landlordName} подтвердил ваше бронирование. Адрес: ${apartment.address}"
                else
                    "Нажмите кнопку ниже, чтобы ответить на вопросы и подтвердить бронирование времени просмотра с собственником.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (!message.isViewingConfirmed) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("confirm_viewing_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Подтвердить запись на просмотр",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GreenMatch,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Запись успешно подтверждена",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPromptChip(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = CoralPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
