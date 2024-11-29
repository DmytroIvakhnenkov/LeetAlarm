package com.example.leetalarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.leetalarm.ui.theme.LeetAlarmTheme
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar


// Main activity of the app, which is the entry point of your application.
class MainActivity : ComponentActivity() {
    // The onCreate function is called when the activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Call the parent class's onCreate method.

        enableEdgeToEdge() // Enables full-screen mode with edge-to-edge content (not shown in this code).

        // Set up the UI content of the activity using Jetpack Compose.
        setContent {
            LeetAlarmTheme { // Applies a custom theme (assumed to be defined elsewhere in your project).
                TimePickerScreen() // Calls a composable function to display the screen with the time picker.
            }
        }
    }
}

// A composable function that displays the time picker screen.
@Composable
fun TimePickerScreen() {
    // A state variable to hold the selected time as a string (e.g., "08:30").
    var selectedTime by remember { mutableStateOf("") }

    // A state variable to control whether the time picker dialog should be shown.
    var showTimePicker by remember { mutableStateOf(false) }

    // Get the current context (needed for dialogs and other Android services).
    val context = LocalContext.current

    // Get the system's AlarmManager service, which is used to schedule alarms.
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // Get the current date and time using a Calendar instance.
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // Get the current hour.
    val currentMinute = calendar.get(Calendar.MINUTE) // Get the current minute.

    // Create a TimePickerDialog for the user to select a time.
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute -> // Callback when the user selects a time.
            // Format the selected time as "HH:mm" (24-hour format) and save it.
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)

            // Update the calendar instance with the selected time.
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0) // Reset seconds to 0.
            calendar.set(Calendar.MILLISECOND, 0) // Reset milliseconds to 0.

            // Schedule the alarm using the selected time.
            setAlarm(context, alarmManager, calendar)
        },
        currentHour, // Set the current hour as the default value.
        currentMinute, // Set the current minute as the default value.
        true // Use 24-hour format.
    )

    // The Scaffold composable provides a basic layout structure.
    Scaffold(
        topBar = {} // Empty top bar, but you can add an AppBar if needed.
    ) { innerPadding ->
        // Create a vertical layout for the content.
        Column(
            modifier = Modifier
                .padding(innerPadding) // Add padding to avoid overlapping with the scaffold.
                .fillMaxSize() // Make the column take up the full screen.
                .padding(16.dp), // Add padding around the column.
            verticalArrangement = Arrangement.Center, // Center the content vertically.
            horizontalAlignment = Alignment.CenterHorizontally // Center the content horizontally.
        ) {
            // Display the selected time or a placeholder message if no time is selected.
            Text(
                text = if (selectedTime.isEmpty()) "No Time Selected" else "Selected Time: $selectedTime",
                style = MaterialTheme.typography.headlineMedium // Use a headline style for the text.
            )

            Spacer(modifier = Modifier.height(16.dp)) // Add space between elements.

            // A button that shows the time picker dialog when clicked.
            Button(onClick = { timePickerDialog.show() }) {
                Text("Pick Time") // Button label.
            }

            FetchJsonScreen("https://alfa-leetcode-api.onrender.com/daily")
        }
    }
}

// A function to set an alarm using the AlarmManager.
fun setAlarm(context: Context, alarmManager: AlarmManager, calendar: Calendar) {
    // Create an intent to send when the alarm triggers.
    val intent = Intent(context, AlarmReceiver::class.java)

    // Wrap the intent in a PendingIntent, which allows it to be executed by the system later.
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0, // Request code (can be used to differentiate multiple alarms).
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags to ensure immutability and update behavior.
    )

    // Schedule the alarm to wake the device and trigger at the selected time.
    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP, // Wake the device if necessary.
        calendar.timeInMillis, // The time to trigger the alarm (in milliseconds).
        pendingIntent // The action to perform when the alarm triggers.
    )

    // Show a toast message to inform the user that the alarm is set.
    Toast.makeText(context, "Alarm set for ${calendar.time}", Toast.LENGTH_SHORT).show()
}

// A BroadcastReceiver that gets triggered when the alarm goes off.
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Show a toast message when the alarm is triggered.
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun FetchJsonScreen(url: String) {
    var responseText by remember { mutableStateOf("Loading...") }

    LaunchedEffect(url) {
        fetchJson(url) { response ->
            responseText = response
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = responseText)
    }
}

fun fetchJson(url: String, callback: (String) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder().url(url).build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            callback("Failed to load data")
        }

        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                val body = response.body?.string() ?: "No response body"
                try {
                    val json = JSONObject(body)
                    val question = json.optString("question", "No question found") // Extract "question" field
                    callback(question)
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback("Error parsing JSON")
                }
            } else {
                callback("Error: ${response.code}")
            }
        }
    })
}
// A preview function to display the TimePickerScreen in Android Studio's design preview.
@Preview(showBackground = true) // Show a background in the preview.
@Composable
fun TimePickerScreenPreview() {
    LeetAlarmTheme {
        TimePickerScreen() // Call the same composable function to render the preview.
    }
}