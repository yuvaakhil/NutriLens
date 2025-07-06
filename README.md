# 🍱 NutriLens – AI-Powered Indian Food Recognition & Nutrition App

**NutriLens** is a smart engineered Android application that uses machine learning to identify Indian food items from images and provides their nutritional information.  
Built using Kotlin, Python (Flask), and Hugging Face models, NutriLens aims to make dietary tracking effortless for Indian meals — often overlooked by generic nutrition apps.

---

## 🚀 Features

- 📸 Upload or capture an image of your food
- 🤖 Identify Indian dishes using ML-based image classification
- 📊 View calorie count, proteins, fats, and carbohydrates
- 🗃️ Track past predictions with timestamps
- ⚡ Fast, clean, and user-friendly interface

---

## 🧠 Tech Stack

### Android App
- **Kotlin**
- **Android Jetpack** (ViewModel, LiveData, Room)
- **Retrofit** for API communication
- **Glide** for image handling

### Backend
- **Python Flask** or **Django REST Framework**
- **Hugging Face** model: `dima806/indian_food_image_detection`
- **Pandas** for matching food items with nutritional dataset
- **SQLite / Excel / CSV** for nutrition data storage

---

## 🔄 Process Flow

1. User captures or uploads a food image
2. Image is sent to backend via API
3. ML model predicts the dish name
4. Nutritional info is fetched from local food database
5. Results are displayed in the app with an option to save

---

![Screenshot 2025-07-06 164035](https://github.com/user-attachments/assets/2ab02f24-3bf6-4d1b-b4ff-b7ecee875410)


---

## 📚 What I Learnt

- End-to-end Android development using Kotlin
- REST API integration and backend model hosting
- ML model deployment (Hugging Face)
- Handling real-world Indian food datasets
- Independent problem-solving and self-directed learning

---

## 🌱 Future Scope

- 🔄 Real-time calorie estimation based on portion size
- 💬 Voice-based logging and feedback
- 🏃 Fitness tracker integration (Google Fit, Fitbit)
- 🧬 AI-based personalized diet plans
- 🌐 Multilingual support for regional Indian languages

---



📄 License
This project is open-source under the MIT License.

🙌 Acknowledgements
Hugging Face - Indian Food Image Model

Android Developers Documentation

Indian Food Nutrition Database- Anuvaad_INDB_2024.11.xlsx 

👨‍💻 Author
Pattela Yuva Akhil

blog- https://medium.com/@yuvaakhil815/nutrilens-using-machine-learning-and-android-to-decode-indian-food-nutrition-aeff391f32de
