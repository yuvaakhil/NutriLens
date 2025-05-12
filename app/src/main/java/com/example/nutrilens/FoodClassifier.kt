package com.example.nutrilens
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import java.io.ByteArrayOutputStream

object FoodClassifier {

    private const val MODEL_URL = "https://api-inference.huggingface.co/models/dima806/indian_food_image_detection"
    private const val HF_API_TOKEN = "hf_FciluvMmSxWRYVJVjpoZdvndpxZA1huDqd"  // Replace this

    suspend fun classifyImage(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                val byteArray = bitmapToByteArray(bitmap)

                val client = OkHttpClient()

                val body = RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray)

                val request = Request.Builder()
                    .url(MODEL_URL)
                    .addHeader("Authorization", HF_API_TOKEN)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val jsonArray = JSONArray(response.body?.string())
                    val topResult = jsonArray.getJSONObject(0)
                    return@withContext topResult.getString("label")
                } else {
                    return@withContext null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
}
