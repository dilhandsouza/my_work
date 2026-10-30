# Python Implementation Guide

Complete guide for using the Python version of Voice to Text Converter with integrated ML model.

## Overview

The Python implementation includes:
- **model.py** - Core ML model class
- **main.py** - Interactive CLI application
- **app.py** - Flask web server (optional)
- **modules/** - Audio recording and transcription

---

## Installation

### Step 1: Install Dependencies

```bash
pip install -r requirements.txt
```

**Required packages:**
- SpeechRecognition - Speech engine
- PyAudio - Microphone interface
- pydub - Audio processing
- Flask - Web server (optional)

### Step 2: System Requirements

**Windows:**
```bash
# May need Visual C++ build tools
# Install from: https://visualstudio.microsoft.com/visual-cpp-build-tools/
```

**macOS:**
```bash
# Install via Homebrew
brew install portaudio
```

**Linux:**
```bash
sudo apt-get install portaudio19-dev
```

---

## Usage

### Quick Start

```bash
python main.py
```

Menu appears:
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

---

## Core Module: VoiceToTextModel

The `model.py` file provides the ML model class that can be imported and used in your own projects.

### Basic Usage

```python
from model import VoiceToTextModel

# Initialize model
model = VoiceToTextModel("my_training.json")

# Add a correction
model.add_training_sample("helo world", "hello world")

# Apply corrections
corrected = model.apply_corrections("helo wrld")
print(corrected)  # Output: hello world

# Check statistics
stats = model.get_model_stats()
print(stats)  # {'status': 'Trained', 'training_samples': 1, 'accuracy': 100}
```

### Class Methods

#### Initialization
```python
model = VoiceToTextModel(data_file="training_data.json")
```

#### Training
```python
# Add a training sample
success = model.add_training_sample(original, corrected)

# Apply corrections to text
corrected_text = model.apply_corrections(text)

# Get model statistics
stats = model.get_model_stats()

# Display formatted statistics
model.display_stats()

# Show learned corrections
model.show_corrections()
```

#### Data Management
```python
# Export training data
json_data = model.export_data()

# Import training data
success = model.import_data(json_string)

# Reset model
model.reset()

# Get training count
count = model.get_training_count()
```

#### Similarity Calculation
```python
# Calculate similarity between strings
similarity = model.calculate_similarity("hello", "hallo")
# Returns: 0.8 (80% similar)

# Get Levenshtein distance
distance = model.levenshtein_distance("cat", "car")
# Returns: 1
```

---

## Using in Your Code

### Example 1: Simple Correction

```python
from model import VoiceToTextModel

model = VoiceToTextModel()

# Teach the model
model.add_training_sample("I went too the store", "I went to the store")

# Use the model
text = model.apply_corrections("I went too the store")
print(text)  # Output: "I went to the store"
```

### Example 2: Batch Processing

```python
from model import VoiceToTextModel
import os

model = VoiceToTextModel("batch_model.json")

# Load corrections from file
corrections = [
    ("helo", "hello"),
    ("wrld", "world"),
    ("woudl", "would"),
]

for original, corrected in corrections:
    model.add_training_sample(original, corrected)

# Process batch of texts
texts = ["helo wrld", "I woudl like helo"]
for text in texts:
    corrected = model.apply_corrections(text)
    print(f"{text} → {corrected}")
```

### Example 3: Model Statistics

```python
from model import VoiceToTextModel

model = VoiceToTextModel()

# Train model
model.add_training_sample("their", "there")
model.add_training_sample("their", "there")
model.add_training_sample("their", "they're")

# Get stats
stats = model.get_model_stats()
print(f"Status: {stats['status']}")
print(f"Samples: {stats['training_samples']}")
print(f"Accuracy: {stats['accuracy']}%")

# Display formatted
model.display_stats()
```

### Example 4: Export and Restore

```python
from model import VoiceToTextModel
import json

# Create and train first model
model1 = VoiceToTextModel("model1.json")
model1.add_training_sample("helo", "hello")

# Export
exported = model1.export_data()

with open("backup.json", "w") as f:
    f.write(exported)

# Create second model and import
model2 = VoiceToTextModel("model2.json")
model2.import_data(exported)

# Both models now know the same corrections
print(model2.apply_corrections("helo"))  # hello
```

---

## Python CLI Application

### Main Features

Interactive menu-driven interface for:
- Recording audio from microphone
- Transcribing audio files
- Training the ML model
- Viewing model statistics
- Managing training data

### Workflow Example

```
Starting Voice to Text CLI...
[Menu displayed]

Select an option: 1
🎤 Recording audio... (press Ctrl+C to stop)
[Speak into microphone]
... stopping recording ...

✅ Audio saved: recording_20260221_103000.wav
🔄 Transcribing...

========================================
📝 Original Transcription:
I went too the store
========================================

🤖 ML Corrected Text:
I went to the store
========================================

Edit the text if needed (press Enter to keep): [User edits or presses Enter]

💾 Save this correction to train the model? (y/n): y
✅ Model learned this correction!

🤖 Model Statistics
========================================
Status:              Trained
Training Samples:    1
Model Accuracy:      100%
Correction Patterns: 1
========================================
```

---

## Testing

Run the unit tests:

```bash
python -m unittest test_model.py -v
```

Tests cover:
- Adding training samples
- Applying corrections
- Similarity calculation
- Levenshtein distance
- Model accuracy
- Export/import functionality
- Edge cases

---

## Advanced: Using as a Module

### Import into Your Project

```python
# my_app.py
from model import VoiceToTextModel
from modules.transcriber import AudioTranscriber

model = VoiceToTextModel()
transcriber = AudioTranscriber()

# Your custom application logic
def process_audio(file_path):
    # Transcribe
    text = transcriber.transcribe(file_path)
    
    # Apply ML corrections
    corrected = model.apply_corrections(text)
    
    # Train if needed
    if needs_training:
        model.add_training_sample(text, corrected)
    
    return corrected
```

### Create Custom Training Pipeline

```python
from model import VoiceToTextModel
from modules.transcriber import AudioTranscriber

def create_custom_model(audio_files, corrections):
    """Create a model from audio files and corrections"""
    
    model = VoiceToTextModel("custom.json")
    transcriber = AudioTranscriber()
    
    for audio_file, correction in zip(audio_files, corrections):
        original = transcriber.transcribe(audio_file)
        model.add_training_sample(original, correction)
    
    model.display_stats()
    return model
```

---

## Configuration

### Model Storage

Change where training data is saved:

```python
# Use custom file
model = VoiceToTextModel("my_custom_model.json")

# Use in temporary location
import tempfile
model = VoiceToTextModel(os.path.join(tempfile.gettempdir(), "temp_model.json"))
```

### Language Settings

Change speech recognition language:

**In modules/transcriber.py:**
```python
recognizer.language = "es-ES"  # Spanish
```

### Accuracy Threshold

Modify similarity threshold in main.py:

```python
if similarity > 0.7:  # Change this value
    correct_count += 1
```

---

## Performance Tips

1. **Efficient Loading**
   ```python
   # Load once, use many times
   model = VoiceToTextModel()
   for text in large_list:
       model.apply_corrections(text)
   ```

2. **Batch Processing**
   ```python
   # Process multiple texts efficiently
   corrected_texts = [model.apply_corrections(t) for t in texts]
   ```

3. **Memory Management**
   ```python
   # For very large training sets
   model.reset()  # Clear if memory is an issue
   ```

---

## Troubleshooting

### Import Errors

```
ModuleNotFoundError: No module named 'model'
```

**Solution:** Ensure you're in the correct directory (voice_to_text/)

### Recording Issues

```
OSError: [Errno -9999] Unanticipated host error
```

**Solution:**
- Check microphone connection
- Install PyAudio correctly for your OS
- Try: `pip install --upgrade pyaudio`

### Transcription Failures

```
No internet connection required (uses specified API)
```

**For Google Speech API:**
- Requires internet connection
- May have rate limits
- Alternative: Use offline engines (setup required)

### Model Not Saving

```
PermissionError: [Errno 13] Permission denied
```

**Solution:**
- Check directory permissions
- Use absolute path
- Run from different directory

---

## Advanced Topics

### Custom Similarity Metrics

Modify `calculate_similarity()` for your use case:

```python
def calculate_similarity(self, str1: str, str2: str) -> float:
    # Use different algorithm
    # Example: Jaro-Winkler similarity
    pass
```

### Extending the Model

Add methods to VoiceToTextModel:

```python
class MyCustomModel(VoiceToTextModel):
    def custom_correction(self, text):
        # Your custom logic
        pass
```

### Integration with Other Systems

```python
# Example: REST API integration
from flask import Flask, jsonify
from model import VoiceToTextModel

app = Flask(__name__)
model = VoiceToTextModel()

@app.route('/correct', methods=['POST'])
def correct_text():
    text = request.json['text']
    corrected = model.apply_corrections(text)
    return jsonify({'corrected': corrected})
```

---

## API Reference

### VoiceToTextModel

**Attributes:**
- `training_data`: List of training samples
- `correction_dictionary`: Dict of learned corrections
- `data_file`: Path to JSON file

**Methods:**
- `add_training_sample(original, corrected)` → bool
- `apply_corrections(text)` → str
- `calculate_similarity(str1, str2)` → float
- `calculate_accuracy()` → int
- `get_model_stats()` → dict
- `export_data()` → str
- `import_data(json_string)` → bool
- `reset()` → None
- `display_stats()` → None
- `show_corrections()` → None

---

## Examples Repository

More examples available in `examples/` directory (coming soon).

---

## Contributing

To contribute improvements:
1. Create feature branch
2. Make changes
3. Add tests
4. Submit pull request

---

Happy coding! 🐍✨
