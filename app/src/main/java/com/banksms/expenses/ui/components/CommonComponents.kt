package com.banksms.expenses.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Hotel
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.banksms.expenses.data.analytics.CategoryBreakdownRow
import com.banksms.expenses.data.analytics.MonthPeriod
import com.banksms.expenses.data.analytics.TrendMonthPoint
import com.banksms.expenses.data.model.AccountEntity
import kotlin.math.max

fun iconForKey(key: String): ImageVector {
    return when (key) {
        "restaurant" -> Icons.Rounded.Restaurant
        "shopping_cart" -> Icons.Rounded.ShoppingCart
        "directions_car" -> Icons.Rounded.DirectionsCar
        "flight" -> Icons.Rounded.Flight
        "hotel" -> Icons.Rounded.Hotel
        "shopping_bag" -> Icons.Rounded.ShoppingBag
        "receipt_long" -> Icons.AutoMirrored.Rounded.ReceiptLong
        "subscriptions" -> Icons.Rounded.Subscriptions
        "home" -> Icons.Rounded.Home
        "account_balance" -> Icons.Rounded.AccountBalance
        "verified_user" -> Icons.Rounded.VerifiedUser
        "local_hospital" -> Icons.Rounded.LocalHospital
        "school" -> Icons.Rounded.School
        "movie" -> Icons.Rounded.Movie
        "local_atm" -> Icons.Rounded.LocalAtm
        "money_off" -> Icons.Rounded.MoneyOff
        "payments" -> Icons.Rounded.Payments
        "trending_up" -> Icons.AutoMirrored.Rounded.TrendingUp
        "replay" -> Icons.Rounded.Replay
        "swap_horiz" -> Icons.Rounded.SwapHoriz
        "savings" -> Icons.Rounded.Savings
        "help_outline" -> Icons.AutoMirrored.Rounded.HelpOutline
        else -> Icons.Rounded.Category
    }
}

@Composable
fun AccountSelectorRow(
    accounts: List<AccountEntity>,
    selectedAccountIds: Set<Long>,
    onToggleAccount: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedAccountIds.isEmpty(),
                onClick = { onToggleAccount(null) },
                label = { Text("All accounts") }
            )
        }
        items(accounts, key = { it.id }) { acc ->
            val selected = acc.id in selectedAccountIds
            FilterChip(
                selected = selected,
                onClick = { onToggleAccount(acc.id) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(acc.accentColorHex))
                    )
                },
                label = {
                    Text(
                        text = acc.displayName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}

@Composable
fun CategoryDonutChart(
    items: List<CategoryBreakdownRow>,
    modifier: Modifier = Modifier
) {
    val total = items.sumOf { it.amount }.coerceAtLeast(0.01)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Canvas(modifier = modifier.size(140.dp)) {
        val strokeWidth = 26.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
        val arcSize = Size(diameter, diameter)

        if (items.isEmpty()) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            return@Canvas
        }

        var startAngle = -90f
        items.forEach { row ->
            val sweep = ((row.amount / total) * 360.0).toFloat().coerceAtLeast(2f)
            drawArc(
                color = Color(row.colorHex),
                startAngle = startAngle,
                sweepAngle = sweep - 1.5f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

@Composable
fun TrendsBarChart(
    points: List<TrendMonthPoint>,
    selectedPeriod: MonthPeriod?,
    showNetSavingsLine: Boolean,
    stackedCategories: Boolean,
    onSelectPeriod: (MonthPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    val incomeColor = Color(0xFF0D9488)
    val expenseColor = Color(0xFFDC2626)
    val netLineColor = Color(0xFF2563EB)
    val selectedBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .pointerInput(points) {
                    detectTapGestures { offset ->
                        val slotWidth = size.width.toFloat() / points.size
                        val idx = (offset.x / slotWidth).toInt().coerceIn(0, points.lastIndex)
                        onSelectPeriod(points[idx].period)
                    }
                }
        ) {
            val maxVal = max(
                points.maxOfOrNull { max(it.income, it.expense) } ?: 1.0,
                1.0
            ).toFloat()
            val slotW = size.width / points.size
            val chartH = size.height - 16.dp.toPx()
            val baseY = chartH + 8.dp.toPx()

            // Horizontal reference lines
            listOf(0.25f, 0.5f, 0.75f, 1f).forEach { frac ->
                val y = baseY - chartH * frac
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val netPoints = mutableListOf<Offset>()

            points.forEachIndexed { idx, pt ->
                val slotLeft = idx * slotW
                if (selectedPeriod == pt.period) {
                    drawRect(
                        color = selectedBg,
                        topLeft = Offset(slotLeft, 0f),
                        size = Size(slotW, size.height)
                    )
                }

                val barW = (slotW * 0.30f).coerceIn(6.dp.toPx(), 18.dp.toPx())
                val center = slotLeft + slotW / 2f

                if (stackedCategories) {
                    var currentTop = baseY
                    val expTotalH = (pt.expense.toFloat() / maxVal) * chartH
                    val sumCat = pt.categoryBreakdown.sumOf { it.amount }.coerceAtLeast(1.0)
                    pt.categoryBreakdown.forEach { cat ->
                        val segH = ((cat.amount / sumCat).toFloat() * expTotalH).coerceAtLeast(1f)
                        drawRect(
                            color = Color(cat.colorHex),
                            topLeft = Offset(center - barW, currentTop - segH),
                            size = Size(barW * 2f, segH)
                        )
                        currentTop -= segH
                    }
                } else {
                    val incH = ((pt.income.toFloat() / maxVal) * chartH).coerceAtLeast(0f)
                    val expH = ((pt.expense.toFloat() / maxVal) * chartH).coerceAtLeast(0f)

                    if (incH > 0f) {
                        drawRect(
                            color = incomeColor,
                            topLeft = Offset(center - barW - 2.dp.toPx(), baseY - incH),
                            size = Size(barW, incH)
                        )
                    }
                    if (expH > 0f) {
                        drawRect(
                            color = expenseColor,
                            topLeft = Offset(center + 2.dp.toPx(), baseY - expH),
                            size = Size(barW, expH)
                        )
                    }
                }

                val clampedNet = pt.net.toFloat().coerceAtLeast(0f)
                val netY = baseY - (clampedNet / maxVal) * chartH
                netPoints += Offset(center, netY)
            }

            if (showNetSavingsLine && netPoints.size >= 2) {
                val path = Path().apply {
                    moveTo(netPoints.first().x, netPoints.first().y)
                    for (i in 1 until netPoints.size) {
                        lineTo(netPoints[i].x, netPoints[i].y)
                    }
                }
                drawPath(
                    path = path,
                    color = netLineColor,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
                netPoints.forEach { p ->
                    drawCircle(color = netLineColor, radius = 3.5.dp.toPx(), center = p)
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val step = if (points.size > 7) 2 else 1
            points.forEachIndexed { idx, pt ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (idx % step == 0 || idx == points.lastIndex) {
                        Text(
                            text = pt.label.take(3),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedPeriod == pt.period) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            LegendDot(color = incomeColor, label = "Income")
            LegendDot(color = expenseColor, label = "Expense")
            if (showNetSavingsLine) LegendDot(color = netLineColor, label = "Net Savings")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
