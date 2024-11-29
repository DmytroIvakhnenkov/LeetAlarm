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
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        // Check if the context is not null (important to avoid NullPointerException).
        if (context != null) {
            // Get today's date in the format used for filenames
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val todayFileName = "${today.format(formatter)}.txt"

            // Access the app's internal files directory
            val filesDir = context.filesDir
            val files = filesDir.listFiles { file -> file.extension == "txt" } // List all .txt files

            // Check for files other than today's file
            var fileExists = false
            files?.forEach { file ->
                if (file.name == todayFileName) {
                    fileExists = true // Today's file already exists
                } else {
                    file.delete() // Delete other files
                }
            }

            if (fileExists) {
                // Do not fetch JSON if today's file already exists
                Toast.makeText(context, "File for today already exists. No need to fetch.", Toast.LENGTH_LONG).show()
                return
            }

            // URL to fetch the JSON data from. Replace this with the actual API endpoint.
            val url = "https://alfa-leetcode-api.onrender.com/daily"

            // Call the fetchJson function to perform a network request and fetch JSON data.
            fetchJson(context, url) { result ->
                // Handle the fetched JSON result using a callback.
                // Since network requests happen on a background thread, UI updates must be done on the main thread.
                android.os.Handler(context.mainLooper).post {
                    // Show a toast message displaying the fetched data.
                    Toast.makeText(context, "Fetched data: $result", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}



fun fetchJson(context: Context, url: String, callback: (String) -> Unit) {
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
                    val json = try {
                        JSONObject(body) // Try parsing the JSON object
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        callback("Error parsing JSON body: ${e.message}")
                        return
                    }

                    // Extract "question" field, with a default value in case it's missing
                    val question = try {
                        json.optString("question", "No question found")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback("Error extracting 'question' field: ${e.message}")
                        return
                    }

                    // Clean the "question" string by removing HTML tags
                    val cleanQuestion = try {
                        question.replace(Regex("<[^>]*>"), "")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback("Error cleaning 'question': ${e.message}")
                        return
                    }

                    // Get today's date in the desired format
                    val today = try {
                        LocalDate.now()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback("Error getting today's date: ${e.message}")
                        return
                    }

                    val formatter = try {
                        DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        callback("Error creating date formatter: ${e.message}")
                        return
                    }

                    val fileName = try {
                        "${today.format(formatter)}.txt"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback("Error formatting today's date: ${e.message}")
                        return
                    }

                    // Write the clean string to the file
                    val file = try {
                        File(context.filesDir, fileName)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback("Error creating file object: ${e.message}")
                        return
                    }

                    try {
                        file.writeText(cleanQuestion)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        callback("Error writing to file: ${e.message}")
                        return
                    }

                    // Success, pass the clean question to the callback
                    callback(cleanQuestion)

                } catch (e: Exception) {
                    e.printStackTrace()
                    callback("An unexpected error occurred: ${e.message}")
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