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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
    val context = LocalContext.current
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val calendar = Calendar.getInstance()
    var currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    var currentMinute = calendar.get(Calendar.MINUTE)


    val timePickerDialog = TimePickerDialog(
        context,
        {_, hourOfDay, minute->
            selectedTime = String.format("%02d:%02d", hourOfDay, minute)
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            setAlarm(context, alarmManager, calendar)
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

fun setAlarm(context: Context, alarmManager: AlarmManager, calendar: Calendar)
{
    val intent = Intent(context, AlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    alarmManager.setAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
    Toast.makeText(context, "Alarm set for ${calendar.time}", Toast.LENGTH_SHORT).show()
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Alarm Triggered!", Toast.LENGTH_LONG).show()
    }
}
fun showNotification(context: Context) {
    val channelId = "alarm_channel"
    val notificationId = 1

    // Create the notification channel (required for Android 8.0+)
    val channel = NotificationChannel(
        channelId,
        "Alarm Notifications",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Channel for alarm notifications"
    }
    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(channel)


    // Build the notification
    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle("Alarm Triggered")
        .setContentText("Your alarm is ringing!")
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()

    // Show the notification
    with(NotificationManagerCompat.from(context)) {
        notify(notificationId, notification)
    }
}

@Preview(showBackground = true)
@Composable
fun TimePickerScreenPreview() {
    LeetAlarmTheme {
        TimePickerScreen() // Calls the same function to render the preview.
    }
}