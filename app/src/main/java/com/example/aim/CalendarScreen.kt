


import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState


import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.platform.LocalContext

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CalendarScreen() {
    var pickedDate by remember { mutableStateOf(LocalDate.now()) }
    var pickedTime by remember { mutableStateOf(LocalTime.NOON) }

    val formattedDate by remember {
        derivedStateOf {
            DateTimeFormatter.ofPattern("MMM dd yyyy").format(pickedDate)
        }
    }
    val formattedTime by remember {
        derivedStateOf {
            DateTimeFormatter.ofPattern("hh:mm").format(pickedTime)
        }
    }

    val dateDialogState = rememberMaterialDialogState()
    val timeDialogState = rememberMaterialDialogState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { dateDialogState.show() }) {
            Text("Pick date")
        }
        Text(text = formattedDate)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { timeDialogState.show() }) {
            Text("Pick time")
        }
        Text(text = formattedTime)
    }

    MaterialDialog(dialogState = dateDialogState, buttons = {
        positiveButton("OK")
        negativeButton("Cancel")
    }) {
        datepicker(
            initialDate = LocalDate.now(),
            title = "Pick a date"
        ) { pickedDate = it }
    }

    MaterialDialog(dialogState = timeDialogState, buttons = {
        positiveButton("OK")
        negativeButton("Cancel")
    }) {
        timepicker(
            initialTime = LocalTime.NOON,
            title = "Pick a time"
        ) { pickedTime = it }
    }
}
