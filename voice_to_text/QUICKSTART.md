# Voice to Text Converter - Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Install Dependencies
```bash
pip install -r requirements.txt
```

### Step 2: Start the Server
```bash
python app.py
```

You'll see:
```
════════════════════════════════════════════════════════════
🚀 Starting Voice to Text Flask Server
════════════════════════════════════════════════════════════
📍 Open http://localhost:5000 in your browser
```

### Step 3: Open in Browser
Click or visit: **http://localhost:5000**

---

## 🎤 Using the App

1. **Record**: Click "🎤 Start Recording" and speak
2. **Transcribe**: Text appears automatically (using Web Speech API)
3. **Correct**: Edit the text if needed
4. **Train**: Click "✓ Save Correction" → model learns from it
5. **Improve**: Next time similar errors appear, model fixes them automatically

---

## 📊 Check Model Status

The app displays:
- **Training Samples**: Corrections learned
- **Accuracy**: How well the model performs
- **Dictionary Size**: Unique corrections stored

---

## 📥 Export / Import Model

- **Export**: Save your trained model → download JSON file
- **Import**: Load a previously saved model → upload JSON file

---

## 🔄 Reset

Click "🔄 Reset Model" to start fresh (clears all training data).

---

## 🛠️ For Developers

### CLI Interface
```bash
python main.py
```
Menu-driven interface for batch processing.

### Run Tests
```bash
python -m unittest test_model.py -v
```

### API Debugging
```bash
# Health check
curl http://localhost:5000/api/health

# Get model stats
curl http://localhost:5000/api/model/stats

# Test correction
curl -X POST http://localhost:5000/api/model/correct \
  -H "Content-Type: application/json" \
  -d '{"text":"i went too the store"}'
```

---

## ❓ Troubleshooting

| Issue | Solution |
|-------|----------|
| "Port already in use" | Change port in `app.py` or kill process using port 5000 |
| "Microphone not working" | Check browser microphone permissions |
| "Module not found" | Run `pip install -r requirements.txt` again |
| "Model not learning" | Ensure text is different, use "Save Correction" button |

---

## 📚 Full Documentation
See [DEPLOYMENT.md](DEPLOYMENT.md) for complete API docs and production setup.

---

**Enjoy! 🎉**
