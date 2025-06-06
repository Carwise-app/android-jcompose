package com.carwise.android.view.price_prediction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun StepIndicator(
    currentStep: Int,
    steps: List<String>,
    stepIcons: List<ImageVector>,
    tint: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, _ ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    // Icon Container
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    index < currentStep -> tint.copy(alpha = 0.2f)
                                    index == currentStep -> tint
                                    else -> Color.LightGray.copy(alpha = 0.2f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                index < currentStep -> Icons.Default.Check
                                else -> stepIcons[index]
                            },
                            contentDescription = steps[index],
                            tint = when {
                                index < currentStep -> tint
                                index == currentStep -> Color.White
                                else -> Color.Gray
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Connector Line
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                when {
                                    index < currentStep -> tint
                                    else -> Color.LightGray.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
            }
        }
    }
} 