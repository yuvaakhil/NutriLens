
package com.example.nutrilens

import android.content.Context
import android.net.Uri
import android.util.Log
import okhttp3.*
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject

object FoodClassifier {

    fun classifyFood(context: Context, imageUri: Uri, callback: (String) -> Unit) {
        val file = uriToFile(context, imageUri)
        val client = OkHttpClient()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image", file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("http://192.168.29.121:5000/predict") // <-- Replace with actual IP if running on real device
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FoodClassifier", "Request Failed: ${e.message}")
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e("FoodClassifier", "Unexpected code $response")
                        callback("Unexpected response code: ${response.code}")
                    } else {
                        val responseBody = response.body?.string()
                        Log.d("FoodClassifier", "Response: $responseBody")
                        val result = parsePrediction(responseBody)
                        callback(result)
                    }
                }
            }
        })
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun parsePrediction(responseBody: String?): String {
        if (responseBody.isNullOrEmpty()) return "No response from server"

        return try {
            // Replace all occurrences of :NaN with :null to avoid JSON parsing errors
            val fixedJson = responseBody.replace(":NaN", ":null")

            val jsonObject = JSONObject(fixedJson)

            val label = jsonObject.optString("label", "Unknown")
            val confidence = jsonObject.optDouble("confidence", 0.0)

            val nutrients = jsonObject.optJSONObject("nutrients")
            val foodName = nutrients?.optString("food_name", "N/A")
            val calories = nutrients?.optDouble("energy_kcal", -1.0)
            val protein = nutrients?.optDouble("protein_g", -1.0)
            val fat = nutrients?.optDouble("fat_g", -1.0)
            val carbs = nutrients?.optDouble("carb_g", -1.0)

            """
        🍛 Food: $foodName
        📊 Confidence: ${"%.2f".format(confidence * 100)}%
        
        🔬 Nutrients (per 100g):
        • Calories: ${if (calories != null && calories >= 0) "%.1f".format(calories) else "N/A"} kcal
        • Protein: ${if (protein != null && protein >= 0) "%.1f".format(protein) else "N/A"} g
        • Fat: ${if (fat != null && fat >= 0) "%.1f".format(fat) else "N/A"} g
        • Carbs: ${if (carbs != null && carbs >= 0) "%.1f".format(carbs) else "N/A"} g
        """.trimIndent()

        } catch (e: Exception) {
            Log.e("FoodClassifier", "Parsing Error: ${e.message}")
            "Failed to parse response"
        }
    }




}
