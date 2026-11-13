package com.droidspaces.app.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * The heading that groups the cards under it. Four screens had grown their own
 * version of this, disagreeing on the style and the weight, so this is the one
 * the design language documents.
 *
 * Spacing stays at the call site through [modifier], because a header in a form
 * needs room above it while one at the top of a settings group does not.
 */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.4.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
