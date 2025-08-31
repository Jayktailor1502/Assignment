package com.example.smartexpensetracker.ui.screens

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.smartexpensetracker.ui.vm.ExpenseViewModel
import kotlinx.coroutines.launch

@Composable
fun ExpenseReportScreen(vm: ExpenseViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Last 7 days (mocked)")
        Spacer(Modifier.height(12.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val barWidth = size.width / 8f
            for (i in 0 until 7) {
                val left = i * barWidth + 8
                val top = size.height * (0.2f + 0.6f * (i / 7f))
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF64B5F6), Color(0xFF1976D2))
                    ),
                    topLeft = Offset(left, top),
                    size = Size(barWidth * 0.7f, size.height - top),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        val ctx = LocalContext.current
        val scope = rememberCoroutineScope()
        Button(onClick = {
            scope.launch {
                val file = vm.exportLast7DaysCsvFile(ctx)
                val uri = FileProvider.getUriForFile(
                    ctx,
                    "${ctx.packageName}.provider",
                    file
                )
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                ctx.startActivity(Intent.createChooser(sendIntent, "Share report"))
            }
        }) {
            Text("Export & Share CSV")
        }
    }
}