from flask import Flask, request, jsonify
from transformers import AutoProcessor, AutoModelForImageClassification
from PIL import Image
import torch
import os
from werkzeug.utils import secure_filename
import pandas as pd

app = Flask(__name__)

# Load model and processor
MODEL_NAME = "dima806/indian_food_image_detection"
processor = AutoProcessor.from_pretrained(MODEL_NAME)
model = AutoModelForImageClassification.from_pretrained(MODEL_NAME)

# Load nutrition data
EXCEL_PATH = r"C:\Users\yuvaa\Desktop\smart diary\SmartFoodDiary\Anuvaad_INDB_2024.11.xlsx"
try:
    df = pd.read_excel(EXCEL_PATH)
    df['food_name'] = df['food_name'].str.lower().str.strip()
except Exception as e:
    print(f"Error loading Excel: {e}")
    df = pd.DataFrame()

UPLOAD_FOLDER = "uploads"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route("/predict", methods=["POST"])
def predict():
    if "image" not in request.files:
        return jsonify({"error": "No image uploaded"}), 400

    image_file = request.files["image"]
    filename = secure_filename(image_file.filename)
    file_path = os.path.join(UPLOAD_FOLDER, filename)
    image_file.save(file_path)

    try:
        image = Image.open(file_path).convert("RGB")

        inputs = processor(images=image, return_tensors="pt")
        with torch.no_grad():
            outputs = model(**inputs)

        logits = outputs.logits
        predicted_class_idx = logits.argmax(-1).item()
        predicted_label = model.config.id2label[predicted_class_idx]
        confidence = torch.nn.functional.softmax(logits, dim=-1)[0][predicted_class_idx].item()

        # Match nutrition details
        matched_row = df[df['food_name'] == predicted_label.lower()]
        if not matched_row.empty:
            nutrients = {
                "food_name": matched_row.iloc[0]["food_name"],
                "energy_kcal": matched_row.iloc[0].get("energy_kcal", "N/A"),
                "protein_g": matched_row.iloc[0].get("protein_g", "N/A"),
                "fat_g": matched_row.iloc[0].get("fat_g", "N/A"),
                "carb_g": matched_row.iloc[0].get("carb_g", "N/A")
            }
        else:
            nutrients = {"food_name": predicted_label, "energy_kcal": None, "protein_g": None, "fat_g": None, "carb_g": None}

        result = {
            "label": predicted_label,
            "confidence": round(confidence, 4),
            "nutrients": nutrients
        }

        return jsonify(result)

    except Exception as e:
        return jsonify({"error": str(e)}), 500

    finally:
        if os.path.exists(file_path):
            os.remove(file_path)

# Run on your local IP
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
