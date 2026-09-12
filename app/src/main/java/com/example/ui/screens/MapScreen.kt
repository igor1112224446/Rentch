package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApartmentEntity
import com.example.ui.components.DrawableResolver
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenMatch
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapScreen(
    apartments: List<ApartmentEntity>,
    onSelectApartment: (ApartmentEntity) -> Unit,
    onStartChat: (ApartmentEntity) -> Unit,
    onLikeApartment: (ApartmentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedApartment by remember { mutableStateOf<ApartmentEntity?>(null) }
    var selectedDistrictFilter by remember { mutableStateOf("Все районы") }

    val districts = listOf("Все районы", "Ваке", "Сабуртало", "Вера", "Мтацминда", "Чугурети", "Дидубе", "Исани", "Ортачала")

    // Map Pan and Zoom State
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Coordinates bounding box for Tbilisi center
    // Lat: 41.67 to 41.76
    // Lng: 44.73 to 44.86
    val minLat = 41.6700
    val maxLat = 41.7600
    val minLng = 44.7400
    val maxLng = 44.8550

    val filteredApartments = remember(apartments, selectedDistrictFilter) {
        if (selectedDistrictFilter == "Все районы") {
            apartments
        } else {
            apartments.filter { it.district.equals(selectedDistrictFilter, ignoreCase = true) }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE5E9F0))
            .testTag("map_screen")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Canvas Map Layer (Roads, River Mtkvari, Parks, District Regions)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.8f, 2.5f)
                        panOffset += pan
                    }
                }
        ) {
            val canvasW = size.width
            val canvasH = size.height

            // Background terrain
            drawRect(color = Color(0xFFEFF2F7))

            // River Mtkvari (Кура) path winding from north-west to south-east
            val riverPath = Path().apply {
                moveTo(canvasW * 0.20f, 0f)
                cubicTo(
                    canvasW * 0.35f, canvasH * 0.25f,
                    canvasW * 0.52f, canvasH * 0.45f,
                    canvasW * 0.48f, canvasH * 0.65f
                )
                cubicTo(
                    canvasW * 0.45f, canvasH * 0.80f,
                    canvasW * 0.70f, canvasH * 0.90f,
                    canvasW * 0.85f, canvasH * 1.0f
                )
            }
            drawPath(
                path = riverPath,
                color = Color(0xFFA5C8E4),
                style = Stroke(width = 32f * zoomScale, cap = StrokeCap.Round)
            )

            // Major thoroughfares (Rustaveli, Chavchavadze, Kazbegi)
            val roadsPath = Path().apply {
                // Chavchavadze -> Melikishvili -> Rustaveli
                moveTo(canvasW * 0.05f, canvasH * 0.60f)
                lineTo(canvasW * 0.32f, canvasH * 0.55f)
                lineTo(canvasW * 0.45f, canvasH * 0.58f)

                // Kazbegi & Pekini in Saburtalo
                moveTo(canvasW * 0.15f, canvasH * 0.28f)
                lineTo(canvasW * 0.38f, canvasH * 0.38f)

                // Tsereteli in Didube
                moveTo(canvasW * 0.38f, canvasH * 0.15f)
                lineTo(canvasW * 0.46f, canvasH * 0.35f)

                // Left embankment
                moveTo(canvasW * 0.48f, canvasH * 0.35f)
                lineTo(canvasW * 0.75f, canvasH * 0.85f)
            }
            drawPath(
                path = roadsPath,
                color = Color(0xFFD6DCE5),
                style = Stroke(width = 12f * zoomScale, cap = StrokeCap.Round)
            )
        }

        // Tbilisi District Labels Overlay
        DistrictLabel(name = "Ваке", xFactor = 0.20f, yFactor = 0.65f, zoom = zoomScale, pan = panOffset)
        DistrictLabel(name = "Сабуртало", xFactor = 0.22f, yFactor = 0.35f, zoom = zoomScale, pan = panOffset)
        DistrictLabel(name = "Вера", xFactor = 0.40f, yFactor = 0.52f, zoom = zoomScale, pan = panOffset)
        DistrictLabel(name = "Мтацминда", xFactor = 0.42f, yFactor = 0.68f, zoom = zoomScale, pan = panOffset)
        DistrictLabel(name = "Чугурети", xFactor = 0.60f, yFactor = 0.45f, zoom = zoomScale, pan = panOffset)
        DistrictLabel(name = "Дидубе", xFactor = 0.42f, yFactor = 0.20f, zoom = zoomScale, pan = panOffset)
        DistrictLabel(name = "Исани", xFactor = 0.78f, yFactor = 0.75f, zoom = zoomScale, pan = panOffset)
        DistrictLabel(name = "Ортачала", xFactor = 0.65f, yFactor = 0.88f, zoom = zoomScale, pan = panOffset)

        // Interactive Apartment Pins
        filteredApartments.forEach { apt ->
            // Map GPS (lat, lng) to screen coords
            val normalizedX = ((apt.lng - minLng) / (maxLng - minLng)).toFloat().coerceIn(0.1f, 0.9f)
            val normalizedY = (1f - ((apt.lat - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0.1f, 0.9f)

            val pinX = (normalizedX * widthPx * zoomScale) + panOffset.x
            val pinY = (normalizedY * heightPx * zoomScale) + panOffset.y

            val isSelected = selectedApartment?.id == apt.id

            Box(
                modifier = Modifier
                    .offset { IntOffset(pinX.roundToInt() - 40, pinY.roundToInt() - 20) }
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { selectedApartment = apt }
                    .shadow(if (isSelected) 8.dp else 4.dp, RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) CoralPrimary else MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
                    .border(
                        2.dp,
                        if (isSelected) Color.White else CoralPrimary,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("pin_${apt.id}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else CoralPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "$${apt.priceUsd}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Top District Filter Bar
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 12.dp),
            color = Color.Transparent
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(districts) { district ->
                    FilterChip(
                        selected = selectedDistrictFilter == district,
                        onClick = {
                            selectedDistrictFilter = district
                            selectedApartment = null
                        },
                        label = { Text(district) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CoralPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        }

        // Map Controls (Zoom In / Out)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 70.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(2.5f) }) {
                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Приблизить")
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                IconButton(onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.8f) }) {
                    Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Отдалить")
                }
            }
        }

        // Floating Bottom Preview Card for Selected Apartment
        selectedApartment?.let { apt ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.94f)
                    .padding(bottom = 16.dp)
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .testTag("map_preview_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val imageRes = DrawableResolver.getDrawableId(context, apt.imageDrawableName)
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = apt.title,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${apt.priceUsd}/мес",
                                color = CoralPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            IconButton(
                                onClick = { selectedApartment = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Закрыть",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = apt.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${apt.district} • ${apt.rooms} комн. • ${apt.areaSqM} м²",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ElevatedButton(
                                onClick = { onSelectApartment(apt) },
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Детали", fontSize = 11.sp)
                            }

                            ElevatedButton(
                                onClick = { onStartChat(apt) },
                                modifier = Modifier.height(34.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Диалог", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DistrictLabel(
    name: String,
    xFactor: Float,
    yFactor: Float,
    zoom: Float,
    pan: Offset
) {
    BoxWithConstraints {
        val x = (xFactor * constraints.maxWidth.toFloat() * zoom) + pan.x
        val y = (yFactor * constraints.maxHeight.toFloat() * zoom) + pan.y

        Box(
            modifier = Modifier
                .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = name,
                fontSize = (11 * zoom).coerceIn(10f, 15f).sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A5568)
            )
        }
    }
}
