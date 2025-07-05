package com.example.nutrilens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import com.example.nutrilens.ui.theme.NutriLensTheme
import androidx.core.content.FileProvider

data class Prediction(val imageUri: Uri, val result: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NutriLensTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    var showHistory by remember { mutableStateOf(false) }
    val predictionHistory = remember { mutableStateListOf<Prediction>() }

    if (showHistory) {
        HistoryScreen(historyList = predictionHistory) {
            showHistory = false
        }
    } else {
        MainScreen(
            onAddPrediction = { predictionHistory.add(it) },
            onViewHistory = { showHistory = true }
        )
    }
}

@Composable
fun MainScreen(
    onAddPrediction: (Prediction) -> Unit,
    onViewHistory: () -> Unit
) {
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var classificationResult by remember { mutableStateOf<String?>(null) }
    val photoFile = remember { mutableStateOf<File?>(null) }

    // Gallery picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        classificationResult = null
    }

    // Camera capture
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedImageUri = photoFile.value?.let {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    it
                )
            }
            classificationResult = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("NutriLens 🍱", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("🖼️ Select from Gallery")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            val file = createImageFile(context)
            photoFile.value = file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            takePictureLauncher.launch(uri)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("📷 Capture from Camera")
        }

        Spacer(modifier = Modifier.height(20.dp))

        selectedImageUri?.let { uri ->
            val bitmap = remember(uri) {
                val inputStream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.LightGray, shape = RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("Image selected: ${uri.lastPathSegment}", fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    FoodClassifier.classifyFood(context, uri) { result ->
                        classificationResult = result
                        onAddPrediction(Prediction(uri, result))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔍 Predict Food")
            }
        }

        classificationResult?.let {
            Spacer(modifier = Modifier.height(20.dp))
            Text("Prediction Result:", fontWeight = FontWeight.Bold)
            Text(it)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("📊 View History")
        }
    }
}

fun createImageFile(context: Context): File {
    return File.createTempFile(
        "temp_image",
        ".jpg",
        context.cacheDir
    ).apply {
        createNewFile()
    }
}

@Composable
fun HistoryScreen(historyList: List<Prediction>, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📜 Prediction History", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(16.dp))

        historyList.reversed().forEach { prediction ->
            Text("Result: ${prediction.result}", fontSize = 16.sp)

            val context = LocalContext.current
            val bitmap = remember(prediction.imageUri) {
                val inputStream = context.contentResolver.openInputStream(prediction.imageUri)
                BitmapFactory.decodeStream(inputStream)
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "History Image",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(4.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("🔙 Back")
        }
    }
}
