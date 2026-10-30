# Voice to Text Converter

A Python application for converting voice and audio files to text using state-of-the-art speech recognition technology. Now with **machine learning capabilities** that improve accuracy over time!

## ✨ Features

### Core Functionality
- Real-time speech-to-text conversion
- Audio file transcription
- Multiple language support
- Easy-to-use interface

### 🤖 Machine Learning Model
- **Learns from corrections** - The model learns from your corrections and improves automatically
- **Adaptive predictions** - Automatically fixes common errors based on learned patterns
- **Word-level dictionary** - Builds a dictionary of common misrecognitions
- **Neural network** - Uses TensorFlow.js for advanced pattern recognition
- **Model statistics** - View training progress and accuracy metrics
- **Export/import** - Save and share your trained model data

### Data Management
- Save corrected text and teach the model
- View model accuracy and training samples
- Export training data as JSON
- Import training data to restore previous learning
- Reset model when needed

## 🚀 Getting Started

### Requirements
- Modern web browser (Chrome, Edge, Safari, Opera)
- Microphone for recording
- No installation needed!

## 📖 Usage - Web Interface

### Basic Workflow
1. **Open** `index.html` in your browser
2. **Click** "Start Recording" button
3. **Speak** clearly into your microphone
4. **Click** "Stop Recording" when done
5. **Edit** the transcribed text if needed
6. **Save corrections** to teach the model
7. **Copy** or **Download** your text

### Model Training
1. Make recordings and transcribe audio
2. Edit any errors in the text
3. Click "Save Correction" when prompted
4. Model learns this correction automatically
5. Future similar errors are corrected automatically

### Model Management
- **View Stats** - Check model status, training samples, and accuracy (top section)
- **Export Data** - Save your trained model to JSON file for backup
- **Import Data** - Restore previously saved training data
- **Reset Model** - Clear all training data and start fresh

## 🐍 Usage - Python Application

```bash
# Install dependencies
pip install -r requirements.txt

# Run the application
python main.py
```

### Options
1. **Record and transcribe** - Record from microphone in real-time
2. **Transcribe file** - Convert existing audio files to text
3. **Exit** - Close the application

## 📁 Project Structure

```
voice_to_text/
├── index.html              # Web interface
├── script.js               # ML model + speech recognition
├── styles.css              # UI styling
├── main.py                 # Python CLI application
├── requirements.txt        # Python dependencies
├── README.md              # This file
├── QUICK_REFERENCE.md     # Quick usage guide
├── ML_MODEL_GUIDE.md      # Detailed ML documentation
└── modules/               # Python modules
    ├── recorder.py        # Audio recording
    ├── transcriber.py     # Speech transcription
    └── __init__.py
```

## 🤖 How the ML Model Works

### Training
- When you correct transcribed text, the model learns the pattern
- Multiple corrections for the same word are tracked
- The model builds a correction dictionary
- A neural network learns complex patterns

### Correction Application
- Future transcriptions are checked against learned patterns
- Common errors are automatically corrected
- Multi-word corrections improve prediction accuracy
- Original capitalization is preserved

### Accuracy
- Model accuracy is calculated based on learned patterns
- Shown as a percentage (0-100%)
- Improves with more corrections
- Visible in the model statistics section

## 🔧 Technical Stack

### Web Interface
- **HTML5** - Structure and layout
- **CSS3** - Modern styling and animations
- **JavaScript** - Speech API and DOM manipulation
- **TensorFlow.js** - Neural network for pattern recognition
- **Web Speech API** - Browser-based speech recognition

### Python Backend
- **SpeechRecognition** - Speech-to-text conversion
- **PyAudio** - Audio input/output
- **pydub** - Audio processing

## 📈 Model Architecture

The ML model uses:
- **Word-level correction dictionary** - Fast lookup for common corrections
- **Neural Network** - 4-layer fully connected network
- **Levenshtein Distance** - Text similarity calculation
- **TensorFlow.js** - Browser-based execution

## 💾 Data Storage

- Training data stored in browser's **localStorage**
- Persists across browser sessions
- Can be exported as JSON and imported later
- ~5-10MB storage limit per domain
- All data remains on your device (no cloud)

## 🌐 Supported Languages

Default: English (US)

Other languages:
- English (UK, US, Canada, Australia)
- Spanish, French, German, Italian
- Japanese, Chinese, Korean
- And many more...

To change, edit `script.js`:
```javascript
recognition.lang = 'es-ES';  // Spanish
```

## 📚 Documentation

- **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** - Quick usage guide and tips
- **[ML_MODEL_GUIDE.md](ML_MODEL_GUIDE.md)** - Detailed ML model documentation
- **Browser Compatibility** - Chrome and Edge recommended

## ✅ Browser Support

| Browser | Status | Notes |
|---------|--------|-------|
| Chrome | ✅ Excellent | Best performance |
| Edge | ✅ Excellent | Full support |
| Safari | ✅ Good | Works on macOS and iOS |
| Opera | ✅ Good | Chromium-based |
| Firefox | ⚠️ Limited | Partial speech API support |
| IE | ❌ Not supported | Too old |

## 🎯 Best Practices

1. **Quality Audio** - Use a good microphone for better recognition
2. **Clear Speech** - Speak clearly and at normal pace
3. **Train Consistently** - Teach the model your unique speech patterns
4. **Reduce Noise** - Minimize background noise
5. **Export Regularly** - Backup your training data
6. **Monitor Stats** - Check accuracy metrics and training progress

## 🔐 Privacy

- All processing happens in your browser
- No data sent to external servers
- No tracking or analytics
- Complete control over your data
- Can reset or export at any time

## 📝 License

MIT License - Feel free to use, modify, and distribute

## 🤝 Contributing

Ideas for improvements:
- Additional language support
- Phrase-level pattern recognition
- Cloud synchronization
- Mobile app version
- Integration with other services

## 🐛 Troubleshooting

### Microphone not working
- Check browser permissions
- Test microphone in other applications
- Try different browser
- Restart browser

### Model not improving
- Ensure you're clicking "Save Correction"
- Check localStorage is enabled
- Try resetting and retraining
- Clear browser cache

### Low transcription accuracy
- Use clearer speech
- Reduce background noise
- Try different microphone
- Ensure browser supports Speech API

## 📞 Support

For issues and questions:
1. Check the documentation files
2. Review browser console for errors
3. Try resetting the model
4. Test with different microphone

## 🎉 Getting Started Now

1. Open `index.html` in your browser
2. Click "Start Recording"
3. Speak into your microphone
4. Correct any errors
5. Click "Save Correction" to teach the model
6. Watch the model accuracy improve!

Happy transcribing! 🎤📝

