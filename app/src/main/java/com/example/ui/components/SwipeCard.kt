package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GreenMatch
import com.example.ui.theme.RedNope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeCard(
    apartment: ApartmentEntity,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onShowDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var isExpanded by remember { mutableStateOf(false) }

    // Swipe sensitivity threshold
    val swipeThreshold = 280f

    val rotation = (dragOffset.value.x / 20f).coerceIn(-25f, 25f)
    val likeAlpha = ((dragOffset.value.x / swipeThreshold)).coerceIn(0f, 1f)
    val nopeAlpha = ((-dragOffset.value.x / swipeThreshold)).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .offset { IntOffset(dragOffset.value.x.roundToInt(), dragOffset.value.y.roundToInt()) }
            .rotate(rotation)
            .pointerInput(apartment.id) {
                detectDragGestures(
                    onDragEnd = {
                        coroutineScope.launch {
                            if (dragOffset.value.x > swipeThreshold) {
                                dragOffset.animateTo(
                                    Offset(1200f, dragOffset.value.y),
                                    spring()
                                )
                                onSwipeRight()
                            } else if (dragOffset.value.x < -swipeThreshold) {
                                dragOffset.animateTo(
                                    Offset(-1200f, dragOffset.value.y),
                                    spring()
                                )
                                onSwipeLeft()
                            } else {
                                dragOffset.animateTo(Offset.Zero, spring())
                            }
                        }
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            dragOffset.animateTo(Offset.Zero, spring())
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            dragOffset.snapTo(dragOffset.value + dragAmount)
                        }
                    }
                )
            }
            .testTag("swipe_card_${apartment.id}")
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Apartment photo
                val imageRes = DrawableResolver.getDrawableId(context, apartment.imageDrawableName)
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = apartment.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Bottom gradient scrim for high readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.2f),
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black.copy(alpha = 0.95f)
                                ),
                                startY = 300f
                            )
                        )
                )

                // Top badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // District badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CoralPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = apartment.district,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Price tag
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralPrimary,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = "$${apartment.priceUsd}/мес",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Bottom Content
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Title and info button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = apartment.title,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = onShowDetails,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Подробнее о квартире",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Address
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = apartment.address,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Key Specs tags (Rooms, Area, Floor)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpecPill(text = "${apartment.rooms} комн.")
                        SpecPill(text = "${apartment.areaSqM} м²")
                        SpecPill(text = apartment.floor)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Feature Chips (Furniture, Pets, Rental period)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FeatureChip(
                            icon = Icons.Default.Chair,
                            text = if (apartment.hasFurniture) "С мебелью" else "Без мебели",
                            isHighlighted = apartment.hasFurniture
                        )
                        FeatureChip(
                            icon = Icons.Default.Pets,
                            text = if (apartment.petsAllowed) "С питомцами" else "Без питомцев",
                            isHighlighted = apartment.petsAllowed
                        )
                        FeatureChip(
                            icon = Icons.Default.DateRange,
                            text = apartment.minPeriod,
                            isHighlighted = false
                        )
                    }
                }

                // Interactive Swipe Stamp: RENTCH (Right)
                if (likeAlpha > 0.05f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 24.dp, top = 60.dp)
                            .rotate(-15f)
                            .border(3.dp, GreenMatch.copy(alpha = likeAlpha), RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f * likeAlpha), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = GreenMatch.copy(alpha = likeAlpha),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "RENTCH!",
                                color = GreenMatch.copy(alpha = likeAlpha),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                // Interactive Swipe Stamp: NOPE (Left)
                if (nopeAlpha > 0.05f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 24.dp, top = 60.dp)
                            .rotate(15f)
                            .border(3.dp, RedNope.copy(alpha = nopeAlpha), RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f * nopeAlpha), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = RedNope.copy(alpha = nopeAlpha),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NOPE",
                                color = RedNope.copy(alpha = nopeAlpha),
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecPill(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.15f),
        contentColor = Color.White
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun FeatureChip(
    icon: ImageVector,
    text: String,
    isHighlighted: Boolean
) {
    val bgColor = if (isHighlighted) CoralPrimary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f)
    val contentColor = if (isHighlighted) CoralPrimary else Color.White

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
