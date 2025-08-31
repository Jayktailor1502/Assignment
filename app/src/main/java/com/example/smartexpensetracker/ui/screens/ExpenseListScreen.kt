package com.example.smartexpensetracker.ui.screens

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.smartexpensetracker.data.model.Expense
import com.example.smartexpensetracker.ui.vm.ExpenseViewModel
import com.example.smartexpensetracker.utils.startOfDayMillis
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(vm: ExpenseViewModel, navController: NavHostController) {
    val selectedDate by vm.selectedDate.collectAsState()
    val expenses by vm.expensesForDate.collectAsState()

    ExpenseListContent(
        expenses = expenses,
        selectedDate = selectedDate,
        navController = navController,
        onDateChange = { newDate -> vm.selectDate(newDate) }
    )
}

@Composable
fun ExpenseListContent(
    expenses: List<Expense>,
    selectedDate: Long,
    navController: NavHostController,
    onDateChange: (Long) -> Unit
) {
    val ctx = LocalContext.current
    val sdfTime = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val sdfDate = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var groupByCategory by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(12.dp)
    ) {
        // 🔹 Top actions row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Row {
                OutlinedButton(onClick = {
                    // open date picker
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = selectedDate
                    DatePickerDialog(
                        ctx,
                        { _, y, m, d ->
                            cal.set(y, m, d, 0, 0, 0)
                            onDateChange(cal.timeInMillis.startOfDayMillis())
                            Log.i("TAG", "ExpenseListScreen: $selectedDate")
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) { Text("${sdfDate.format(Date(selectedDate))}") }

                Spacer(Modifier.width(8.dp))
                Spacer(Modifier.width(8.dp))

                Button(onClick = {
                    navController.navigate("entry") {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }) {
                    Text("Add")
                }
            }
        }

        // 🔹 Totals row
        if (expenses.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total: ${expenses.size} items")
                Text("₹${String.format("%.2f", expenses.sumOf { it.amount })}")
            }
        }

        // 🔹 Toggle group mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Group by:")
            Row {
                FilterChip(
                    selected = !groupByCategory,
                    onClick = { groupByCategory = false },
                    label = { Text("Time") }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = groupByCategory,
                    onClick = { groupByCategory = true },
                    label = { Text("Category") }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 🔹 Expenses list
        if (expenses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No expenses for this date")
            }
        } else {
            val grouped: Map<String, List<Expense>> =
                if (groupByCategory) expenses.groupBy { it.category }
                else expenses.groupBy { sdfTime.format(Date(it.timestamp)) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                grouped.forEach { (header, items) ->
                    item {
                        Text(
                            header,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                        )
                    }
                    items(items) { e ->
                        ExpenseCard(e, sdfTime)
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(expense: Expense, sdfTime: SimpleDateFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Title + Category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3
                )
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Right side: Amount + Time
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = sdfTime.format(Date(expense.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}