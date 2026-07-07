package com.daming.fushengzhi2.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daming.fushengzhi2.ui.theme.MingColors

@Composable
fun MingCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MingColors.BgWhite),
        border = BorderStroke(1.dp, MingColors.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MingColors.BgPanel)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(title, color = MingColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (subtitle != null) Text(subtitle, color = MingColors.TextMuted, fontSize = 12.sp)
    }
}

@Composable
fun MingButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, danger: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (danger) MingColors.Red else MingColors.Primary,
            disabledContainerColor = MingColors.Border
        )
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ResourcePill(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xCCFFFCF0), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, color = MingColors.TextMuted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(formatNumber(value), color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun EmptyHint(text: String) {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, color = MingColors.TextMuted, fontSize = 14.sp)
    }
}

@Composable
fun DividerLine() {
    Spacer(Modifier.fillMaxWidth().height(1.dp).background(MingColors.Border))
}

fun formatNumber(n: Int): String {
    val abs = kotlin.math.abs(n)
    val sign = if (n < 0) "-" else ""
    return when {
        abs >= 10000 -> sign + String.format("%.1fw", abs / 10000.0)
        abs >= 1000 -> sign + String.format("%.1fk", abs / 1000.0)
        else -> n.toString()
    }
}
