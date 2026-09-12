package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionnaireDialog(
    initialProfile: UserProfileEntity?,
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        phone: String,
        rentalPeriod: String,
        tenantsCount: Int,
        hasPets: Boolean,
        petType: String,
        preferredDistrict: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialProfile?.name.orEmpty().ifBlank { "Алексей" }) }
    var phone by remember { mutableStateOf(initialProfile?.phone.orEmpty().ifBlank { "+995 599 000 111" }) }

    // a) Период аренды: на месяц, от месяца до года, от года
    var selectedPeriod by remember {
        mutableStateOf(initialProfile?.rentalPeriod ?: "От месяца до года")
    }
    val periodOptions = listOf("На месяц", "От месяца до года", "От года")

    // b) Сколько человек будет проживать?
    var tenantsCount by remember { mutableIntStateOf(initialProfile?.tenantsCount ?: 1) }

    // c) Есть ли домашние животные?
    var selectedPetOption by remember {
        mutableStateOf(
            if (initialProfile?.hasPets == true) initialProfile.petType else "Нет животных"
        )
    }
    val petOptions = listOf("Нет животных", "Кошка", "Собака", "Другое животное")

    // d) В каком районе вы хотели бы снять жилье? (районы Тбилиси)
    var selectedDistrict by remember {
        mutableStateOf(initialProfile?.preferredDistrict ?: "Все районы")
    }
    val districtOptions = listOf(
        "Все районы",
        "Ваке",
        "Сабуртало",
        "Вера",
        "Мтацминда",
        "Дидубе",
        "Чугурети",
        "Исани",
        "Ортачала"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("questionnaire_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Регистрация & Опросник",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Для подтверждения просмотра и подбора вариантов",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Questions
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Question A: Период аренды
                    QuestionSection(
                        number = "1",
                        icon = Icons.Default.DateRange,
                        title = "На какой период вы хотите арендовать жильё?"
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            periodOptions.forEach { period ->
                                SelectableChip(
                                    text = period,
                                    isSelected = selectedPeriod == period,
                                    onClick = { selectedPeriod = period }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question B: Количество человек
                    QuestionSection(
                        number = "2",
                        icon = Icons.Default.Group,
                        title = "Сколько человек будет проживать?"
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(1, 2, 3, 4).forEach { count ->
                                val label = if (count == 4) "4+ чел." else "$count чел."
                                SelectableChip(
                                    text = label,
                                    isSelected = tenantsCount == count,
                                    onClick = { tenantsCount = count }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question C: Животные
                    QuestionSection(
                        number = "3",
                        icon = Icons.Default.Pets,
                        title = "Есть ли у вас домашние животные?"
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            petOptions.forEach { pet ->
                                SelectableChip(
                                    text = pet,
                                    isSelected = selectedPetOption == pet,
                                    onClick = { selectedPetOption = pet }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Question D: Районы Тбилиси
                    QuestionSection(
                        number = "4",
                        icon = Icons.Default.LocationOn,
                        title = "В каком районе вы хотели бы снять жильё?"
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            districtOptions.forEach { district ->
                                SelectableChip(
                                    text = district,
                                    isSelected = selectedDistrict == district,
                                    onClick = { selectedDistrict = district }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Contact fields for Registration
                    QuestionSection(
                        number = "5",
                        icon = Icons.Default.Person,
                        title = "Ваши контактные данные"
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Ваше имя") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_user_name"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Номер телефона (WhatsApp / звонки)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_user_phone"),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Submit Action
                Button(
                    onClick = {
                        val hasPets = selectedPetOption != "Нет животных"
                        onSubmit(
                            name.ifBlank { "Арендатор" },
                            phone.ifBlank { "+995 599 123 456" },
                            selectedPeriod,
                            tenantsCount,
                            hasPets,
                            selectedPetOption,
                            selectedDistrict
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_questionnaire_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Подтвердить и записаться на просмотр",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionSection(
    number: String,
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(CoralContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number,
                    color = CoralPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CoralPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        content()
    }
}

@Composable
private fun SelectableChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (isSelected) CoralContainer else MaterialTheme.colorScheme.surface
    val textColor = if (isSelected) CoralPrimary else MaterialTheme.colorScheme.onSurface

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = CoralPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}
