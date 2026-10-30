# Machine Learning Model Documentation

## Overview

This Voice to Text Converter includes an **adaptive machine learning model** that learns from your corrections and improves accuracy over time.

## How It Works

### 1. Initial Speech Recognition
- The Web Speech API transcribes your audio
- The result may contain errors depending on background noise, accent, etc.

### 2. User Correction
- You edit the transcribed text if needed
- The system detects changes automatically
- A correction prompt appears asking if you want to teach the model

### 3. Model Learning
- Click **"Save Correction"** to add the correction to training data
- The model learns the pattern: `original text → corrected text`
- This data is stored locally in your browser's localStorage

### 4. Future Predictions
- When new audio is transcribed, the model checks against learned patterns
- Similar errors are automatically corrected
- The more you train it, the smarter it gets!

## Model Architecture

### Components

1. **Correction Dictionary**
   - Stores word-level corrections with frequency data
   - Fast lookup for common word substitutions
   - Preserves capitalization patterns

2. **Neural Network (TensorFlow.js)**
   - 4-layer fully connected network
   - Learns complex correction patterns
   - Includes dropout for regularization
   - Trained on character-level representations

3. **Similarity Calculator**
   - Uses Levenshtein distance algorithm
   - Measures accuracy of predictions
   - Calculates model accuracy percentage

### Network Architecture

```
Input Layer (128 features)
    ↓
Dense Layer (64 units) + ReLU
    ↓
Dropout (20%)
    ↓
Dense Layer (32 units) + ReLU
    ↓
Dropout (20%)
    ↓
Dense Layer (16 units) + ReLU
    ↓
Output Layer (1 unit) + Sigmoid
```

## Training Process

### Automatic Training
Every time you save a correction:
1. The original and corrected text are compared word by word
2. Different words are identified
3. A mapping is created: `original_word → corrected_word`
4. Frequency of corrections is tracked

### Example
```
Original: "I went too the store"
Corrected: "I went to the store"

Learned mapping:
- "too" → "to" (occurrence: 1)
```

### Multiple Corrections
If multiple corrections exist for the same word:
```
Corrections for "their":
- "their" → "there" (5 times)
- "their" → "their" (2 times)  [already correct]

Best choice: "there" (most frequent correction)
```

## Storage

### LocalStorage Structure
```json
{
  "voiceToTextTraining": [
    {
      "original": "Original transcribed text",
      "corrected": "What you corrected it to",
      "timestamp": "2024-02-21T10:30:00.000Z"
    }
  ]
}
```

### Data Persistence
- Training data is saved automatically in browser's localStorage
- Persists across browser sessions
- ~5-10MB storage limit per domain
- Can be exported and imported

## Performance Metrics

### Model Status
- **Untrained**: 0 training samples, no learned patterns
- **Trained**: 1+ training samples, corrections being applied

### Accuracy
- Calculated using Levenshtein distance similarity
- Measures how well predictions match training data
- Updated automatically when corrections are saved
- Shown as a percentage (0-100%)

### Training Samples
- Counts the number of correction pairs learned
- More samples generally = better accuracy
- Each unique correction increments the count

## Practical Examples

### Example 1: Common Misheard Word
```
User says: "I need too go"
Transcribed: "I need two go"
You correct to: "I need to go"
Click "Save Correction"

Next time user says: "I need too go"
Transcribed: "I need two go"
Model applies: "I need to go" ✓ [automatically fixed!]
```

### Example 2: Homophones
```
Multiple corrections taught:
- "their" → "their" (kept as is)
- "their" → "there" (corrected 3 times)
- "their" → "they're" (corrected 2 times)

Model remembers:
"their" most commonly becomes "there" in your speech
```

## Customization

### Change Language
Edit `script.js`:
```javascript
recognition.lang = 'es-ES';  // Spanish
recognition.lang = 'fr-FR';  // French
```

### Adjust Model Complexity
Modify the neural network in `script.js` `buildModel()` method:
```javascript
// Add more layers for complex patterns
tf.layers.dense({ inputShape: [128], units: 128, activation: 'relu' }),
tf.layers.dense({ units: 64, activation: 'relu' }),
// ... etc
```

### Change Storage Key
Modify in `script.js`:
```javascript
localStorage.setItem('customKeyName', JSON.stringify(this.trainingData));
```

## Advanced Features

### Export/Import Workflow
1. **Export**: Save training data as JSON file
   - Transfer model between devices
   - Create backups
   - Share improvements

2. **Import**: Load training data
   - Restore previous training
   - Share model improvements
   - Combine datasets

### Model Reset
- Clears all training data
- Resets neural network weights
- Starts completely fresh
- Useful for testing or cleaning up

## Limitations & Considerations

### Current Limitations
1. Browser-only (no cloud sync)
2. localStorage limited to ~5-10MB
3. Simple word-level corrections only
4. No phrase-level contextual understanding
5. Requires browser to have localStorage enabled

### Future Improvements
- Phrase-level pattern recognition
- Contextual understanding using LSTM networks
- Cloud synchronization
- Multi-language support
- Custom training parameters

## Technical Details

### Levenshtein Distance
Used to calculate text similarity:
```javascript
// Example
distance("cat", "cart") = 1
similarity = (4 - 1) / 4 = 0.75 (75%)
```

### Character Encoding
Text is converted to character codes for neural network input:
```javascript
"hello" → [104, 101, 108, 108, 111]
```

### Normalization
Character codes are normalized to 0-1 range:
```javascript
charCode / 255  // Range 0-1
```

## Best Practices

1. **Be Consistent**: Save corrections consistently for better learning
2. **Quality Input**: Use clear speech and good microphone
3. **Regular Training**: Teach the model throughout your usage
4. **Backup Data**: Periodically export your training data
5. **Monitor Accuracy**: Check the accuracy percentage
6. **Clean Data**: Reset if accuracy drops unexpectedly

## Troubleshooting

### Model not improving?
- Ensure localStorage is enabled in your browser
- Click "Save Correction" after editing
- Check browser console for errors
- Try resetting and starting fresh

### Accuracy stuck at low percentage?
- Model may need more diverse training samples
- Try teaching it different types of corrections
- Reset and retrain with better data

### Can't import data?
- Ensure JSON file is valid
- Check file format matches export structure
- Try exporting fresh data first to verify format

## Performance Considerations

- Training data stored locally - no server load
- Model runs entirely in browser
- Fast predictions (< 100ms)
- Minimal memory footprint
- Scales well up to ~1000 training samples

## Privacy

- All data stays on your device
- No cloud uploads
- No tracking or analytics
- Complete user control over data
- Can reset or export anytime
