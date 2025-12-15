package com.example.hastanghubaga.feature.today

import android.R
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hastanghubaga.data.local.entity.supplement.DoseAnchorType
import com.example.hastanghubaga.data.local.entity.supplement.FrequencyType
import com.example.hastanghubaga.data.local.entity.supplement.SupplementDoseUnit
import com.example.hastanghubaga.domain.model.supplement.MealAwareDoseState
import com.example.hastanghubaga.domain.model.supplement.Supplement
import com.example.hastanghubaga.domain.model.supplement.SupplementWithUserSettings
import com.example.hastanghubaga.ui.preview.PreviewData
import com.example.hastanghubaga.ui.tokens.AppIcons
import com.example.hastanghubaga.ui.tokens.Dimens
import com.example.hastanghubaga.ui.tokens.UiColors

@Composable
fun TodaySupplementsScreen(
    showBottomSheet: (content: @Composable () -> Unit) -> Unit,
    snackbarData: SnackbarHostState,
    viewModel: TodaySupplementsViewModel
) {
    val state by viewModel.state.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                showBottomSheet {
                    Text("Hello from Today Screen!")
                }
            }) {
                Text("Show Alert Sheet")
            }
        }
        Box(modifier = Modifier.fillMaxSize()){
            when {
                state.isLoading -> {
                    Text("Loading...")
                }
                state.errorMessage != null -> {
                    Text("Error: ${state.errorMessage}")
                }
                else -> {
                    Log.d("Meow", "TodaySupplementsScreen> Rendering ${state.todaySupplements.size} supplements")
                    LazyColumn {
                        items(state.todaySupplements) { supp ->
                            SupplementRow(supp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupplementRow(
    supplement: SupplementWithUserSettings
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceS)
            .border(color = UiColors.Primary(), width = 1.dp)
            .padding(Dimens.SpaceS)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Text(text = supplement.supplement.name)

            Text(text = supplement.supplement.recommendedServingSize.toString())

            supplement.userSettings?.preferredServingSize?.let {
                Text(text = it.toString())
            }

            // --- Avoid caffeine indicator ---
            if (supplement.supplement.avoidCaffeine == true) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = Dimens.SpaceXS)
                ) {
                    Icon(
                        painter = painterResource(
                            id = AppIcons.AvoidCaffeine
                        ),
                        contentDescription = "Avoid caffeine",
                        modifier = Modifier.size(20.dp),
                        tint = UiColors.Warning()
                    )

                    Spacer(modifier = Modifier.width(Dimens.SpaceS))

                    Text(
                        text = "Avoid caffeine",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SupplementRowPreview() {
    MaterialTheme {
        SupplementRow(
            supplement = PreviewData.supplementWithCaffeineWarning
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SupplementRowPreviewDark() {
    MaterialTheme {
    SupplementRow(
        supplement = PreviewData.supplementWithCaffeineWarning
    )
}}

@Preview(showBackground = true)
@Composable
private fun SupplementListPreview() {
    MaterialTheme {
        LazyColumn {
            items(PreviewData.supplementList) { supp ->
                SupplementRow(supp)
            }
        }
    }
}

private fun previewSupplementWithUserSettings(): SupplementWithUserSettings {
    return SupplementWithUserSettings(
        supplement = previewSupplement(),
        userSettings = null,
        doseState = MealAwareDoseState.Unknown,
        scheduledTimes = emptyList()
    )
}

private fun previewSupplement(): Supplement {
    return Supplement(
        id = 1L,
        name = "Vitamin C",
        recommendedServingSize = 2.0,
        recommendedDoseUnit = SupplementDoseUnit.MG,
        avoidCaffeine = true,
        brand = "Brand X",
        notes = "notes",
        servingsPerDay = 1,
        recommendedWithFood = null,
        recommendedLiquidInOz = null,
        recommendedTimeBetweenDailyDosesMinutes = null,
        doseConditions = emptySet(),
        doseAnchorType = DoseAnchorType.MIDNIGHT,
        frequencyType = FrequencyType.DAILY,
        frequencyInterval = null,
        weeklyDays = emptyList(),
        offsetMinutes = null,
        startDate = null,
        lastTakenDate = null,
        ingredients = emptyList(),
        isActive = true,
        // fill only what SupplementRow actually uses
        // everything else can be default / dummy
    )
}