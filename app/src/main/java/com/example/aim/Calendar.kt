package com.example.calendar



import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.calendar.ui.theme.CalendarTheme
import com.example.calendar.ui.theme.CalendarScreen


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarTheme {
                CalendarScreen()
            }
        }
    }
}
