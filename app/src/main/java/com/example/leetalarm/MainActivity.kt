package com.example.leetalarm

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.leetalarm.ui.theme.LeetAlarmTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LeetAlarmTheme {
                TimePickerScreen()
            }
        }
    }
}

@Composable
fun TimePickerScreen()
{
    var selectedTime by remember { mutableStateOf("")}
    var showTimePicker by remember { mutableStateOf(false) }

    val calendar = Calendar.getInstance()
    var currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    var currentMinute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        LocalContext.current,
        {_, hourOfDay, minute->
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)
        },
        currentHour,
        currentMinute,
        true
    )
    Scaffold (
        topBar = {}
    ){ innerPadding ->

        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
            verticalArrangement  = Arrangement.Center,
            horizontalAlignment  = Alignment.CenterHorizontally
        )
        {
            Text(
                text = if (selectedTime.isEmpty()) "No Time Selected" else "Selected Time:$selectedTime",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {timePickerDialog.show()}) {
                Text("Pick Time")
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun TimePickerScreenPreview() {
    LeetAlarmTheme {
        TimePickerScreen() // Calls the same function to render the preview.
    }
}