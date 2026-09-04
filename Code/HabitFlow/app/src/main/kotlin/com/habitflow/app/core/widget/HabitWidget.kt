package com.habitflow.app.core.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import com.habitflow.app.MainActivity
import com.habitflow.app.HabitFlowApplication
import com.habitflow.app.OccurrenceStatus
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class HabitWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as HabitFlowApplication).repository
        
        // Fetch data for today
        val habits = repository.habits.first()
        val occurrences = repository.occurrences.first()
        
        val today = LocalDate.now()
        val todayEpochDay = today.toEpochDay()
        val dayOfWeek = today.dayOfWeek.value
        
        val todayHabits = habits.filter { 
            it.scheduledDays.isEmpty() || it.scheduledDays.split(",").contains(dayOfWeek.toString())
        }
        
        val todayOccurrences = occurrences.filter { it.scheduledEpochDay == todayEpochDay }.associateBy { it.habitId }
        
        val completedCount = todayHabits.count { todayOccurrences[it.id]?.status == OccurrenceStatus.COMPLETED }
        val totalCount = todayHabits.size
        val percentage = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

        provideContent {
            WidgetContent(percentage, todayHabits.map { it.name to (todayOccurrences[it.id]?.status == OccurrenceStatus.COMPLETED) })
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetContent(percentage: Int, habits: List<Pair<String, Boolean>>) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF121212))
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Progress Area
            Column(
                modifier = GlanceModifier.width(80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(60.dp)
                        .background(Color(0xFF39FF14))
                        .cornerRadius(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$percentage%",
                        style = TextStyle(
                            color = ColorProvider(Color.Black),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = "Hoàn thành",
                    style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 10.sp)
                )
            }

            Spacer(GlanceModifier.width(12.dp))

            // Right: Habit List
            Column(modifier = GlanceModifier.defaultWeight().fillMaxHeight()) {
                Text(
                    text = "Thói quen hôm nay",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF39FF14)),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(GlanceModifier.height(6.dp))
                if (habits.isEmpty()) {
                    Text(
                        text = "Không có thói quen nào",
                        style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 11.sp)
                    )
                } else {
                    // Limit to top 5 habits to ensure they are readable
                    val visibleHabits = habits.take(5)
                    Column(modifier = GlanceModifier.defaultWeight().fillMaxWidth()) {
                        visibleHabits.forEachIndexed { index, (name, completed) ->
                            Row(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .fillMaxWidth()
                                    .background(if (completed) Color(0x3339FF14) else Color(0xFF2C2C2E))
                                    .cornerRadius(8.dp)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (completed) "✓" else "○",
                                    style = TextStyle(
                                        color = ColorProvider(if (completed) Color(0xFF39FF14) else Color.White),
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(GlanceModifier.width(6.dp))
                                Text(
                                    text = name,
                                    maxLines = 1,
                                    style = TextStyle(
                                        color = ColorProvider(if (completed) Color.Gray else Color.White),
                                        fontSize = 12.sp,
                                        fontWeight = if (completed) FontWeight.Normal else FontWeight.Medium
                                    )
                                )
                            }
                            if (index < visibleHabits.size - 1) {
                                Spacer(GlanceModifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
