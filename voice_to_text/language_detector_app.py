"""
Flask Web Application for Language Detection & Grammar Correction
Provides a web interface for text analysis
"""

from flask import Flask, render_template, request, jsonify
from langdetect import detect, detect_langs, LangDetectException
from langdetect import DetectorFactory
import requests as http_requests
from datetime import datetime
import os
import json

# Seed for deterministic language detection results
DetectorFactory.seed = 0

app = Flask(__name__)

LANG_MAP = {
    'en': 'en-US',
    'es': 'es',
    'fr': 'fr',
    'de': 'de',
    'it': 'it',
    'pt': 'pt-PT',
    'ru': 'ru',
    'ja': 'ja',
    'zh-cn': 'zh',
    'ko': 'ko',
    'nl': 'nl',
    'pl': 'pl',
    'tr': 'tr',
    'sv': 'sv',
}

LANGUAGE_TOOL_API = 'https://api.languagetool.org/v2/check'

# Language mappings
LANGUAGE_NAMES = {
    'en': 'English',
    'es': 'Spanish',
    'fr': 'French',
    'de': 'German',
    'it': 'Italian',
    'pt': 'Portuguese',
    'ru': 'Russian',
    'ja': 'Japanese',
    'zh-cn': 'Chinese (Simplified)',
    'zh-tw': 'Chinese (Traditional)',
    'ko': 'Korean',
    'hi': 'Hindi',
    'ar': 'Arabic',
    'th': 'Thai',
    'vi': 'Vietnamese',
    'nl': 'Dutch',
    'pl': 'Polish',
    'tr': 'Turkish',
    'sv': 'Swedish',
    'da': 'Danish',
    'fi': 'Finnish',
    'no': 'Norwegian',
    'cs': 'Czech',
    'hu': 'Hungarian',
    'ro': 'Romanian',
    'el': 'Greek',
    'kn': 'Kannada',
    'ta': 'Tamil',
    'te': 'Telugu',
    'ml': 'Malayalam'
}

TRAINING_DATA_FILE = 'training_data.json'

def load_training_data():
    if os.path.exists(TRAINING_DATA_FILE):
        with open(TRAINING_DATA_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)
    return []

def save_training_data(data):
    with open(TRAINING_DATA_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)

def detect_language(text):
    """Detect language from text"""
    try:
        text = text.strip()
        if not text:
            return None, 'Empty', 0.0

        # Need sufficient text for reliable detection
        if len(text.split()) < 3:
            # For very short text, still try but note low reliability
            probabilities = detect_langs(text)
            if probabilities:
                best = probabilities[0]
                # Low confidence for short text - default to English
                if best.prob < 0.70:
                    return 'en', 'English', best.prob
                lang_name = LANGUAGE_NAMES.get(best.lang, best.lang.upper())
                return best.lang, lang_name, best.prob
            return 'en', 'English', 0.5

        probabilities = detect_langs(text)
        if not probabilities:
            return 'en', 'English', 0.5

        # Sort by probability and pick the best
        best = probabilities[0]
        lang_code = best.lang
        confidence = best.prob

        # If confidence is low, default to English
        if confidence < 0.50:
            return 'en', 'English', confidence

        lang_name = LANGUAGE_NAMES.get(lang_code, lang_code.upper())
        return lang_code, lang_name, confidence

    except LangDetectException:
        return 'en', 'English', 0.0

def correct_grammar(text, language_code='en'):
    """Correct grammar using LanguageTool public REST API."""
    if not text or not text.strip():
        return text
    try:
        lt_lang = LANG_MAP.get(language_code, 'en-US')
        response = http_requests.post(
            LANGUAGE_TOOL_API,
            data={'text': text, 'language': lt_lang},
            timeout=10
        )
        if response.status_code != 200:
            print(f'LanguageTool API error: {response.status_code} {response.text}')
            return text
        result = response.json()
        matches = result.get('matches', [])
        if not matches:
            return text
        # Apply corrections in reverse order to preserve offsets
        corrected = text
        for match in reversed(matches):
            replacements = match.get('replacements', [])
            if replacements:
                offset = match['offset']
                length = match['length']
                corrected = corrected[:offset] + replacements[0]['value'] + corrected[offset + length:]
        return corrected
    except Exception as e:
        print(f'Grammar correction error: {e}')
        return text

@app.route('/')
def index():
    """Serve main page"""
    return render_template('language_detector.html')

@app.route('/api/analyze', methods=['POST'])
def analyze():
    """Analyze text for language and grammar"""
    try:
        data = request.json
        text = data.get('text', '').strip()
        
        if not text:
            return jsonify({
                'status': 'error',
                'message': 'Please provide text to analyze'
            }), 400
        
        # Detect language
        lang_code, lang_name, confidence = detect_language(text)
        
        # Correct grammar
        corrected_text = correct_grammar(text, lang_code) if lang_code else text
        
        # Find errors
        has_errors = text != corrected_text
        
        return jsonify({
            'status': 'success',
            'original_text': text,
            'corrected_text': corrected_text,
            'detected_language': lang_name,
            'language_code': lang_code,
            'confidence': f'{confidence * 100:.1f}%',
            'has_errors': has_errors,
            'timestamp': datetime.now().isoformat()
        })
    
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500

@app.route('/api/languages', methods=['GET'])
def get_languages():
    """Get supported languages"""
    return jsonify({
        'status': 'success',
        'languages': list(LANGUAGE_NAMES.values()),
        'total': len(LANGUAGE_NAMES)
    })

@app.route('/api/health', methods=['GET'])
def health():
    """Health check"""
    return jsonify({
        'status': 'success',
        'message': 'Language Detection API is running',
        'timestamp': datetime.now().isoformat()
    })

@app.route('/api/train', methods=['POST'])
def train_model():
    data = request.get_json()
    original_text = data.get('original_text')
    corrected_text = data.get('corrected_text')
    language_code = data.get('language_code')

    if not all([original_text, corrected_text, language_code]):
        return jsonify({'status': 'error', 'message': 'Missing original_text, corrected_text, or language_code'}), 400

    training_data = load_training_data()
    training_data.append({
        'original_text': original_text,
        'corrected_text': corrected_text,
        'language_code': language_code,
        'timestamp': datetime.utcnow().isoformat() + 'Z'
    })
    save_training_data(training_data)

    # In a real application, this would trigger model retraining or fine-tuning.
    # For this demonstration, we are just storing the data.

    return jsonify({'status': 'success', 'message': 'Correction saved for model training'})

if __name__ == '__main__':
    app.run(
        debug=True,
        host='0.0.0.0',
        port=5002,
        use_reloader=True
    )
