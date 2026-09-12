package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FilterState
import com.example.data.model.FurnitureFilter
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralPrimary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    currentFilter: FilterState,
    onDismiss: () -> Unit,
    onApply: (FilterState) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var minPrice by remember { mutableFloatStateOf(currentFilter.minBudget.toFloat()) }
    var maxPrice by remember { mutableFloatStateOf(currentFilter.maxBudget.toFloat()) }
    var selectedFurniture by remember { mutableStateOf(currentFilter.furnitureRequirement) }
    var selectedDistrict by remember { mutableStateOf(currentFilter.district) }
    var petFriendlyOnly by remember { mutableStateOf(currentFilter.petFriendlyOnly) }

    val districts = listOf("Все районы", "Ваке", "Сабуртало", "Вера", "Мтацминда", "Дидубе", "Чугурети", "Исани", "Ортачала")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .testTag("filter_bottom_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Фильтры поиска",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Budget Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Бюджет (в месяц)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$${minPrice.roundToInt()} — $${maxPrice.roundToInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = CoralPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            RangeSlider(
                value = minPrice..maxPrice,
                onValueChange = { range ->
                    minPrice = (range.start / 50).roundToInt() * 50f
                    maxPrice = (range.endInclusive / 50).roundToInt() * 50f
                },
                valueRange = 300f..3000f,
                steps = 53,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Furniture Filter
            Text(
                text = "Наличие мебели",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FurnitureFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFurniture == filter,
                        onClick = { selectedFurniture = filter },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralContainer,
                            selectedLabelColor = CoralPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. District Filter
            Text(
                text = "Район Тбилиси",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                districts.forEach { dist ->
                    FilterChip(
                        selected = selectedDistrict == dist,
                        onClick = { selectedDistrict = dist },
                        label = { Text(dist) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralContainer,
                            selectedLabelColor = CoralPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Pet friendly toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Только с домашними животными",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Показывать объекты, где хозяева разрешают питомцев",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = petFriendlyOnly,
                    onCheckedChange = { petFriendlyOnly = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = CoralPrimary)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons: Reset & Apply
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        minPrice = 300f
                        maxPrice = 3000f
                        selectedFurniture = FurnitureFilter.ANY
                        selectedDistrict = "Все районы"
                        petFriendlyOnly = false
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Сбросить")
                }

                Button(
                    onClick = {
                        onApply(
                            FilterState(
                                minBudget = minPrice.roundToInt(),
                                maxBudget = maxPrice.roundToInt(),
                                district = selectedDistrict,
                                furnitureRequirement = selectedFurniture,
                                petFriendlyOnly = petFriendlyOnly
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Применить", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
