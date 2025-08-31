package com.example.smartexpensetracker.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.smartexpensetracker.data.model.Expense
import com.example.smartexpensetracker.ui.vm.ExpenseViewModel
import com.example.smartexpensetracker.utils.startOfDayMillis
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    vm: ExpenseViewModel,
    navController: NavController
) {
    val ctx = LocalContext.current

    // --- State ---
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var notes by remember { mutableStateOf("") }
    var receiptUri by remember { mutableStateOf<Uri?>(null) }

    // Date state (defaults to today)
    var selectedDate by remember {
        mutableLongStateOf(
            System.currentTimeMillis().startOfDayMillis()
        )
    }

    // --- Expenses for selected date ---
    val expensesForDate by vm.expensesForDate.collectAsState(initial = emptyList())
    val totalForDate = expensesForDate.sumOf { it.amount }

    // 🔥 Collect toast messages
    LaunchedEffect(Unit) {
        vm.toast.collect { msg ->
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🔹 Total spent for date
            Text(
                "Total: ₹${String.format("%.2f", totalForDate)} on ${
                    SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                    ).format(Date(selectedDate))
                }",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // 🔹 Date picker
            OutlinedButton(onClick = {
                val cal = Calendar.getInstance()
                cal.timeInMillis = selectedDate
                DatePickerDialog(
                    ctx,
                    { _, y, m, d ->
                        cal.set(y, m, d, 0, 0, 0)
                        selectedDate = cal.timeInMillis.startOfDayMillis()
                        Log.i("TAG", "ExpenseEntryScreen: $selectedDate")
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }) {
                Text(
                    SimpleDateFormat(
                        "dd MMM yyyy",
                        Locale.getDefault()
                    ).format(Date(selectedDate))
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                )
            )

            // 🔹 Category dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf("Staff", "Travel", "Food", "Utility").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                category = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { if (it.length <= 100) notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // 🔹 Mock receipt upload
            Button(
                onClick = {
                    //receiptUri = "content://fake/receipt.jpg".toUri()
                    Toast.makeText(ctx, "Receipt attached (mock)", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Upload, contentDescription = "Upload")
                Spacer(Modifier.width(8.dp))
                Text(if (receiptUri == null) "Attach Receipt" else "Receipt Attached")
            }

            Spacer(Modifier.height(20.dp))

            // 🔹 Submit
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (title.isBlank() || amt == null || amt <= 0.0) {
                        Toast.makeText(ctx, "Enter valid title and amount", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    val expense = Expense(
                        title = title,
                        amount = amt,
                        category = category,
                        notes = notes,
                        receiptUri = receiptUri?.toString(),
                        timestamp = System.currentTimeMillis(),                 // exact time now
                        date = selectedDate.startOfDayMillis()                  // normalized date
                    )

                    vm.addExpense(expense)

                    // Reset fields
                    title = ""
                    amount = ""
                    category = "Food"
                    notes = ""
                    receiptUri = null
                    selectedDate = System.currentTimeMillis().startOfDayMillis()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        }
    }
}
