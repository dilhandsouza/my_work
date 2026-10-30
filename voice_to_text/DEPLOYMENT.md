# Voice to Text Converter - Flask Deployment Guide

## Quick Start

### Prerequisites
- Python 3.8+
- pip (Python package manager)
- Web browser with microphone access

### Installation & Running

#### 1. Install Dependencies
Open a terminal in the `voice_to_text` directory and run:

```bash
pip install -r requirements.txt
```

This installs:
- Flask 3.0.0 - Web framework
- Werkzeug 3.0.0 - WSGI utilities
- SpeechRecognition 3.10.0 - Audio transcription
- PyAudio 0.2.13 - Audio input device access
- pydub 0.25.1 - Audio format conversion

#### 2. Start the Flask Server
```bash
python app.py
```

Expected output:
```
════════════════════════════════════════════════════════════
🚀 Starting Voice to Text Flask Server
════════════════════════════════════════════════════════════
📍 Open http://localhost:5000 in your browser
🌐 Server: http://0.0.0.0:5000
🤖 ML Model: Ready for training

📊 API Endpoints:
   - GET  /api/health                 - Health check
   - GET  /api/model/stats            - Get model statistics
   - POST /api/model/correct          - Apply ML corrections
   - POST /api/model/train            - Train model
   - GET  /api/model/corrections      - Get learned corrections
   - GET  /api/model/export           - Export training data
   - POST /api/model/import           - Import training data
   - POST /api/model/reset            - Reset model
════════════════════════════════════════════════════════════
```

#### 3. Access the Web Application
Open your browser and navigate to:
```
http://localhost:5000
```

## Using the Web Interface

### Recording Audio
1. Click the **"🎤 Start Recording"** button
2. Speak naturally (the browser uses Web Speech API)
3. The transcribed text appears in the text area
4. The ML model automatically applies learned corrections

### Training the Model
1. If the transcription has errors, edit the text manually
2. Click **"✓ Save Correction"** button
3. The model learns this correction and applies it to future similar words

### Viewing Model Statistics
- **Training Samples**: Number of corrections learned
- **Model Accuracy**: Percentage accuracy on test data
- **Dictionary Size**: Number of unique word corrections
- **Status**: Current model state

### Export & Import Training Data
- **Export**: Click "📥 Export Model" to download training data as JSON
- **Import**: Click "📤 Import Model" to upload previously saved training data

### Reset Model
- Click **"🔄 Reset Model"** to clear all training data (requires confirmation)

## API Documentation

### Base URL
```
http://localhost:5000
```

### Endpoints

#### Health Check
```
GET /api/health
```
**Response:**
```json
{
    "status": "ok",
    "app": "Voice to Text Converter",
    "model_trained": true,
    "timestamp": "2024-01-15T10:30:45.123456"
}
```

#### Get Model Statistics
```
GET /api/model/stats
```
**Response:**
```json
{
    "status": "success",
    "data": {
        "status": "Active",
        "training_samples": 42,
        "accuracy": 87.5,
        "dictionary_size": 15
    },
    "timestamp": "2024-01-15T10:30:45.123456"
}
```

#### Apply Corrections
```
POST /api/model/correct
Content-Type: application/json

{
    "text": "i went too the store"
}
```
**Response:**
```json
{
    "status": "success",
    "original": "i went too the store",
    "corrected": "I went to the store",
    "changed": true,
    "timestamp": "2024-01-15T10:30:45.123456"
}
```

#### Train Model
```
POST /api/model/train
Content-Type: application/json

{
    "original": "i went too the store",
    "corrected": "I went to the store"
}
```
**Response:**
```json
{
    "status": "success",
    "message": "Model trained successfully",
    "stats": {
        "status": "Active",
        "training_samples": 43,
        "accuracy": 88.2,
        "dictionary_size": 16
    },
    "timestamp": "2024-01-15T10:30:45.123456"
}
```

#### Get Learned Corrections
```
GET /api/model/corrections
```
**Response:**
```json
{
    "status": "success",
    "corrections": {
        "too": {
            "correction": "to",
            "count": 5,
            "all_corrections": {
                "to": 5
            }
        },
        "i": {
            "correction": "I",
            "count": 3,
            "all_corrections": {
                "I": 3
            }
        }
    },
    "total": 2,
    "timestamp": "2024-01-15T10:30:45.123456"
}
```

#### Export Training Data
```
GET /api/model/export
```
**Response:** JSON file download (e.g., `voice_model_training_20240115_103045.json`)

#### Import Training Data
```
POST /api/model/import
Content-Type: multipart/form-data

file: <training_data.json>
```
**Response:**
```json
{
    "status": "success",
    "message": "Model imported successfully",
    "stats": { ... },
    "timestamp": "2024-01-15T10:30:45.123456"
}
```

#### Reset Model
```
POST /api/model/reset
```
**Response:**
```json
{
    "status": "success",
    "message": "Model reset successfully",
    "stats": {
        "status": "Active",
        "training_samples": 0,
        "accuracy": 0,
        "dictionary_size": 0
    },
    "timestamp": "2024-01-15T10:30:45.123456"
}
```

## Project Structure

```
voice_to_text/
├── app.py                          # Flask web server
├── model.py                        # ML engine (Python)
├── main.py                         # CLI interface
├── test_model.py                   # Unit tests
├── requirements.txt                # Python dependencies
├── flask_training_data.json        # Model training data (auto-created)
│
├── templates/
│   └── index.html                  # Flask HTML template
│
├── static/
│   ├── css/
│   │   └── styles.css              # CSS styling
│   └── js/
│       └── app.js                  # Flask-integrated JavaScript
│
├── modules/
│   ├── recorder.py                 # Audio recording
│   ├── transcriber.py              # Speech to text
│   └── __init__.py
│
└── [Documentation files]
    ├── README.md
    ├── DEPLOYMENT.md               # This file
    ├── etc.
```

## Troubleshooting

### Port Already in Use
If port 5000 is already in use:
```bash
# Find what process is using port 5000
netstat -ano | findstr :5000

# Change port in app.py line at the bottom:
app.run(debug=True, host='0.0.0.0', port=5001)
```

### Microphone Not Working
1. Ensure your browser has microphone permissions
2. Firefox: Check about:preferences#privacy
3. Chrome: Check chrome://settings/privacy
4. Grant microphone permission when prompted

### Module Not Found Errors
```bash
# Reinstall all requirements
pip install -r requirements.txt --force-reinstall
```

### Model Not Learning
1. Ensure corrections are different from original text
2. Check model statistics via `/api/model/stats`
3. Export and inspect training data via `/api/model/export`

## Alternative Interfaces

### Command-Line Interface (CLI)
```bash
python main.py
```
Provides menu-driven interface for batch processing and model management.

### Static HTML (No Python Server)
Open `index.html` directly in browser for local-only operation (no Python needed).

## Production Deployment

### Using Gunicorn
For production, use Gunicorn WSGI server:

```bash
# Install Gunicorn
pip install gunicorn

# Run with Gunicorn
gunicorn -w 4 -b 0.0.0.0:5000 app:app
```

### Using Docker
Create `Dockerfile`:
```dockerfile
FROM python:3.9-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
CMD ["gunicorn", "-w", "4", "-b", "0.0.0.0:5000", "app:app"]
```

Build and run:
```bash
docker build -t voice-to-text .
docker run -p 5000:5000 voice-to-text
```

### Environment Variables
Configure via environment:
```bash
export FLASK_ENV=production
export FLASK_DEBUG=0
gunicorn app:app
```

## Performance Tips

1. **Enable Model Persistence**: Training data auto-saves to JSON
2. **Batch Processing**: Use CLI for processing multiple files
3. **Caching**: Browser caches static assets
4. **Connection Pool**: Run multiple Gunicorn workers for concurrent requests

## Security Considerations

1. Use HTTPS in production (SSL/TLS certificates)
2. Implement authentication if exposing to public internet
3. Rate limit API endpoints
4. Validate all user inputs
5. Store sensitive training data securely

## Support & Documentation

- **Model Technical Details**: See [model.py](model.py) comments
- **Frontend Code**: See [static/js/app.js](static/js/app.js)
- **Python Tests**: Run `python -m unittest test_model.py -v`
- **API Testing**: Use curl, Postman, or similar tools

## Next Steps

1. ✅ Start Flask server
2. 📝 Train model with usage patterns
3. 📊 Monitor accuracy metrics
4. 🔄 Export trained model periodically
5. 🚀 Deploy to production when ready
