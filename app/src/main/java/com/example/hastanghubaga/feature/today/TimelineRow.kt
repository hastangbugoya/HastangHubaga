package com.example.hastanghubaga.feature.today

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.data.local.entity.meal.MealType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.activity.ActivityType
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.ui.timeline.ActivityUiModel
import com.example.hastanghubaga.ui.timeline.ImportedMealUiModel
import com.example.hastanghubaga.ui.timeline.MealUiModel
import com.example.hastanghubaga.ui.timeline.SupplementDoseLogUiModel
import com.example.hastanghubaga.ui.timeline.SupplementUiModel
import com.example.hastanghubaga.ui.timeline.TimelineItem
import com.example.hastanghubaga.ui.timeline.TimelineItemUiModel
import com.example.hastanghubaga.ui.timeline.icon
import com.example.hastanghubaga.ui.tokens.Dimens
import com.example.hastanghubaga.ui.tokens.UiColors
import kotlinx.datetime.LocalTime

@Composable
fun TimelineRow(
    item: TimelineItemUiModel,
    onClick: (TimelineItemUiModel) -> Unit = {},
    previewForceExpanded: Boolean? = null
) {
    var isExpanded by rememberSaveable(item.key) { mutableStateOf(false) }
    val effectiveExpanded = previewForceExpanded ?: isExpanded

    val isSupplementCard = item is SupplementUiModel || item is SupplementDoseLogUiModel
    val isActivityCard = item is ActivityUiModel

    val supplementIngredients = when (item) {
        is SupplementUiModel -> item.ingredients
        is SupplementDoseLogUiModel -> item.ingredients
        else -> emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceS)
            .border(color = UiColors.Primary(), width = 1.dp)
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = true,
                onClick = {
                    if (isSupplementCard || isActivityCard) {
                        isExpanded = !isExpanded
                    } else {
                        onClick(item)
                    }
                }
            )
            .padding(Dimens.SpaceS)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.time.toDisplayText(),
                    style = MaterialTheme.typography.labelMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.isCompleted) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = item.icon()),
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )

            when (item) {
                is ActivityUiModel -> {
                    item.activityTypeLabel
                        ?.takeIf { it.isNotBlank() }
                        ?.let { activityTypeLabel ->
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = activityTypeLabel,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                }

                else -> {
                    item.subtitle?.let { subtitle ->
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (effectiveExpanded && supplementIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                supplementIngredients.forEach { ingredient ->
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(ingredient.name)
                            }

                            if (ingredient.amountText.isNotBlank()) {
                                append(" ")
                                append(ingredient.amountText)
                            }
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (item is SupplementUiModel && effectiveExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { onClick(item) }
                    ) {
                        Text("Log")
                    }
                }
            }

            if (item is ActivityUiModel && effectiveExpanded) {
                val address = item.addressText?.takeIf { it.isNotBlank() }

                address?.let {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (address != null) {
                        TextButton(
                            onClick = {
                                Log.d("ActivityShare", "Share tapped for ${item.title}")
                            }
                        ) {
                            Text("Share text")
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    TextButton(
                        onClick = { onClick(item) }
                    ) {
                        Text("Log")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineRowPreview_SupplementCollapsed() {
    Surface {
        TimelineRow(
            item = previewSupplementItem(),
            previewForceExpanded = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineRowPreview_SupplementExpanded() {
    Surface {
        TimelineRow(
            item = previewSupplementItem(),
            previewForceExpanded = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineRowPreview_ActivityCollapsed() {
    Surface {
        TimelineRow(
            item = previewActivityItem(),
            previewForceExpanded = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineRowPreview_ActivityExpanded() {
    Surface {
        TimelineRow(
            item = previewActivityItem(),
            previewForceExpanded = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineRowPreview_LocalMeal() {
    Surface {
        TimelineRow(
            item = previewLocalMealItem(),
            previewForceExpanded = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelineRowPreview_ImportedAkMeal() {
    Surface {
        TimelineRow(
            item = previewImportedMealItem(),
            previewForceExpanded = false
        )
    }
}

private fun previewSupplementItem(): SupplementUiModel =
    SupplementUiModel(
        id = 1L,
        time = LocalTime(hour = 8, minute = 0),
        title = "Daily Multivitamin",
        subtitle = "2 capsules",
        isCompleted = false,
        supplementId = 1L,
        scheduledTime = LocalTime(hour = 8, minute = 0),
        doseState = MealAwareDoseState.Ready,
        defaultUnit = SupplementDoseUnit.CAPSULE,
        suggestedDose = 2.0,
        occurrenceId = "supp_occ_preview_1",
        ingredients = listOf(
            TimelineItem.TimelineIngredientUi(
                name = "Vitamin C",
                amountText = "500 mg"
            ),
            TimelineItem.TimelineIngredientUi(
                name = "Magnesium",
                amountText = "75 mg"
            )
        )
    )

private fun previewActivityItem(): ActivityUiModel =
    ActivityUiModel(
        id = 2L,
        time = LocalTime(hour = 7, minute = 0),
        title = "Strength Training",
        subtitle = null,
        isCompleted = false,
        activityId = 1L,
        activityType = ActivityType.STRENGTH_TRAINING,
        activityTypeLabel = "Strength Training",
        addressText = "Gold's Gym Long Beach",
        startTime = LocalTime(hour = 7, minute = 0),
        endTime = LocalTime(hour = 7, minute = 45),
        intensity = 7,
        occurrenceId = "act_occ_preview_1"
    )

private fun previewLocalMealItem(): MealUiModel =
    MealUiModel(
        id = 3L,
        time = LocalTime(hour = 12, minute = 0),
        title = "Lunch",
        subtitle = "Chicken rice bowl",
        isCompleted = false,
        mealId = 1L,
        mealType = MealType.LUNCH,
        occurrenceId = "meal_occ_preview_1"
    )

private fun previewImportedMealItem(): ImportedMealUiModel =
    ImportedMealUiModel(
        id = 4L,
        time = LocalTime(hour = 18, minute = 30),
        title = "Imported Dinner",
        subtitle = "Adobong Kangkong + rice",
        isCompleted = true,
        importedMealId = 101L,
        mealType = MealType.DINNER,
        totalCalories = 540,
        totalProtein = 22.0,
        totalCarbs = 68.0,
        totalFat = 18.0,
        totalSodium = 780.0,
        totalCholesterol = 35.0,
        totalFiber = 6.0
    )