# Quick Reference - Voice to Text Converter

Complete guide for using the voice-to-text application in both web and Python CLI formats.

## Two Ways to Use

### 1️⃣ Web Interface (Browser)
- Real-time speech recognition
- Interactive UI with live corrections
- Model statistics dashboard
- Perfect for: Regular users, real-time usage

### 2️⃣ Python CLI (Command Line)
- Batch processing support
- Advanced model control
- Programmatic access
- Perfect for: Developers, automation, scripting

---

## Installation

### Prerequisites
- Python 3.8+
- Microphone connected to your computer
- Modern web browser (Chrome, Edge, Safari)

### Setup

```bash
# 1. Navigate to project directory
cd voice_to_text

# 2. Install dependencies
pip install -r requirements.txt

# 3. Choose your interface below
```

---

## Web Interface (Browser)

### Quick Start - Static HTML

1. **Simply open** `index.html` in a web browser
   - Double-click the file
   - Or drag into browser window
   - Or use VS Code Live Server extension

2. **Click** "🎙️ Start Recording"
3. **Speak** clearly into your microphone
4. **Stop** recording when done
5. **View** transcribed text
6. **Edit** if needed
7. **Click** "✅ Save Correction" to train model

### Web Server Version (with Python Backend)

For advanced features and API access:

```bash
# Start Flask server
python app.py

# Open browser to: http://localhost:5000
```

### Web Features

- 🎤 Start/Stop recording
- Real-time transcription
- 🤖 Automatic ML corrections
- ✏️ Text editing
- 📊 Model statistics
- 💾 Export/Import model
- 📋 Copy to clipboard
- 📥 Download as .txt

---

## Python CLI Application

### Start Application

```bash
python main.py
```

### Menu Options

```
1. 🎙️  Record and transcribe audio
2. 📁 Transcribe existing audio file
3. 📊 View model statistics
4. 📚 Show learned corrections
5. 💾 Export training data
6. 📂 Import training data
7. 🔄 Reset model
8. ❌ Exit
```

### Quick Workflow

1. Select "Record and transcribe audio"
2. Speak when prompted
3. Review transcription and ML corrections
4. Edit if needed
5. Save correction to train model
6. Repeat!

---

## 🤖 Machine Learning Model

### How It Works

1. **Record** audio and get transcription
2. **Edit** text if there are errors  
3. **Save** the correction
4. **Model** learns this pattern
5. **Future** similar errors auto-correct ✨

### Training Example

```
Correction: "helo" → "hello"
Next transcription includes "helo"
Result: Automatically corrected to "hello"!
```

### Model Statistics

- **Status**: Trained or Untrained
- **Training Samples**: Number of corrections learned
- **Accuracy**: Performance percentage
- **Dictionary Size**: Unique correction patterns

---

## 🌐 Language Support

### Change Language

Edit configuration:
- Web: `script.js` → `recognition.lang = 'es-ES'`
- Python: `modules/transcriber.py` language parameter

### Supported Languages
- `en-US` - English (US)
- `es-ES` - Spanish
- `fr-FR` - French
- `de-DE` - German
- `it-IT` - Italian
- `ja-JP` - Japanese
- `zh-CN` - Chinese
- And many more...

---

## 💾 Data Management

### Export Model
```bash
Web:    Click "Export Data" button
Python: Select menu option 5
```

### Import Model
```bash
Web:    Click "Import Data" button
Python: Select menu option 6, enter filename
```

### Backup & Share
Training data is JSON - easy to backup, share, or combine!

---

## ⚠️ Troubleshooting

### Microphone Issues
- Check browser/system permissions
- Test in other applications
- Try different browser
- Restart browser

### Model Not Learning
- Verify you're saving corrections
- Check data file exists
- Reset and retrain
- Export to verify data

### Low Accuracy
- Speak more clearly
- Reduce background noise
- Use better microphone
- Try different language
- Check accuracy threshold

---

## 📁 Project Files

```
voice_to_text/
├── index.html           # Web interface
├── script.js            # Web ML model
├── styles.css           # Web styling
├── app.py               # Flask server
├── main.py              # Python CLI
├── model.py             # Core ML model
├── test_model.py        # Unit tests
├── modules/
│   ├── recorder.py
│   ├── transcriber.py
│   └── __init__.py
├── requirements.txt
├── README.md
├── ML_MODEL_GUIDE.md
└── QUICK_REFERENCE.md
```

---

## 🎯 Quick Start

**Web:** Open `index.html` → Click record → Speak → Save corrections

**Python:** `python main.py` → Select 1 → Speak → Save corrections

**Flask:** `python app.py` → Open `http://localhost:5000`

---

## 📚 Documentation

- **README.md** - Full project guide
- **ML_MODEL_GUIDE.md** - Technical ML details
- **This file** - Quick reference

Enjoy transcribing! 🎤📝

