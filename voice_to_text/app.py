"""
Web Server for Voice to Text Converter
Flask application providing web interface with Python ML backend
"""

from flask import Flask, render_template, request, jsonify, send_file
from model import VoiceToTextModel
import json
import os
from datetime import datetime
import io

app = Flask(__name__, 
            template_folder='templates',
            static_folder='static',
            static_url_path='/static')

app.config['MAX_CONTENT_LENGTH'] = 50 * 1024 * 1024  # 50MB max file size
app.config['JSON_SORT_KEYS'] = False

# Initialize the ML model
ml_model = VoiceToTextModel("flask_training_data.json")

print("\n" + "="*60)
print("🤖 ML Model Initialized")
print("="*60)
ml_model.display_stats()


@app.route('/')
def index():
    """Serve the main page"""
    return render_template('index.html')


@app.route('/api/model/stats', methods=['GET'])
def get_model_stats():
    """Get current model statistics"""
    try:
        stats = ml_model.get_model_stats()
        return jsonify({
            'status': 'success',
            'data': stats,
            'timestamp': datetime.now().isoformat()
        })
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500


@app.route('/api/model/correct', methods=['POST'])
def apply_corrections():
    """Apply ML corrections to text"""
    try:
        data = request.get_json()
        
        if not data or 'text' not in data:
            return jsonify({
                'status': 'error',
                'message': 'Text parameter required',
                'timestamp': datetime.now().isoformat()
            }), 400
        
        text = str(data.get('text', ''))
        
        if not text.strip():
            return jsonify({
                'status': 'error',
                'message': 'Text cannot be empty',
                'timestamp': datetime.now().isoformat()
            }), 400
        
        # Apply corrections
        corrected_text = ml_model.apply_corrections(text)
        
        return jsonify({
            'status': 'success',
            'original': text,
            'corrected': corrected_text,
            'changed': text != corrected_text,
            'timestamp': datetime.now().isoformat()
        })
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500


@app.route('/api/model/train', methods=['POST'])
def train_model():
    """Train model with a correction pair"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({
                'status': 'error',
                'message': 'Request body required',
                'timestamp': datetime.now().isoformat()
            }), 400
        
        original = str(data.get('original', '')).strip()
        corrected = str(data.get('corrected', '')).strip()
        
        if not original or not corrected:
            return jsonify({
                'status': 'error',
                'message': 'Original and corrected text required (cannot be empty)',
                'timestamp': datetime.now().isoformat()
            }), 400
        
        # Train the model
        if ml_model.add_training_sample(original, corrected):
            stats = ml_model.get_model_stats()
            return jsonify({
                'status': 'success',
                'message': 'Model trained successfully',
                'stats': stats,
                'timestamp': datetime.now().isoformat()
            })
        else:
            return jsonify({
                'status': 'info',
                'message': 'No change detected - not trained',
                'timestamp': datetime.now().isoformat()
            })
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500


@app.route('/api/model/corrections', methods=['GET'])
def get_corrections():
    """Get learned corrections"""
    try:
        corrections = {}
        for original, corr_dict in ml_model.correction_dictionary.items():
            best = max(corr_dict, key=corr_dict.get)
            corrections[original] = {
                'correction': best,
                'count': corr_dict[best],
                'all_corrections': dict(corr_dict)
            }
        
        return jsonify({
            'status': 'success',
            'corrections': corrections,
            'total': len(corrections),
            'timestamp': datetime.now().isoformat()
        })
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


@app.route('/api/model/export', methods=['GET'])
def export_model():
    """Export training data as JSON file"""
    try:
        data = ml_model.export_data()
        
        buffer = io.BytesIO()
        buffer.write(data.encode('utf-8'))
        buffer.seek(0)
        
        return send_file(
            buffer,
            as_attachment=True,
            download_name=f'voice_model_training_{datetime.now().strftime("%Y%m%d_%H%M%S")}.json',
            mimetype='application/json'
        )
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e)
        }), 500


@app.route('/api/model/import', methods=['POST'])
def import_model():
    """Import training data from JSON file"""
    try:
        if 'file' not in request.files:
            return jsonify({
                'status': 'error',
                'message': 'No file provided',
                'timestamp': datetime.now().isoformat()
            }), 400
        
        file = request.files['file']
        
        if file.filename == '':
            return jsonify({
                'status': 'error',
                'message': 'No file selected',
                'timestamp': datetime.now().isoformat()
            }), 400
        
        if not file.filename.endswith('.json'):
            return jsonify({
                'status': 'error',
                'message': 'File must be JSON format',
                'timestamp': datetime.now().isoformat()
            }), 400
        
        content = file.read().decode('utf-8')
        
        if ml_model.import_data(content):
            stats = ml_model.get_model_stats()
            return jsonify({
                'status': 'success',
                'message': 'Model imported successfully',
                'stats': stats,
                'timestamp': datetime.now().isoformat()
            })
        else:
            return jsonify({
                'status': 'error',
                'message': 'Failed to import model',
                'timestamp': datetime.now().isoformat()
            }), 400
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500


@app.route('/api/model/reset', methods=['POST'])
def reset_model():
    """Reset the model"""
    try:
        ml_model.reset()
        
        return jsonify({
            'status': 'success',
            'message': 'Model reset successfully',
            'stats': ml_model.get_model_stats(),
            'timestamp': datetime.now().isoformat()
        })
    except Exception as e:
        return jsonify({
            'status': 'error',
            'message': str(e),
            'timestamp': datetime.now().isoformat()
        }), 500


@app.route('/api/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({
        'status': 'ok',
        'app': 'Voice to Text Converter',
        'model_trained': ml_model.get_training_count() > 0 if hasattr(ml_model, 'get_training_count') else len(ml_model.correction_dictionary) > 0,
        'timestamp': datetime.now().isoformat()
    })


@app.after_request
def after_request(response):
    """Add CORS headers to all responses"""
    response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS, PUT, DELETE'
    response.headers['Access-Control-Allow-Headers'] = 'Content-Type, Authorization'
    return response


@app.errorhandler(400)
def bad_request(error):
    """Handle 400 Bad Request"""
    return jsonify({
        'status': 'error',
        'message': 'Bad request',
        'timestamp': datetime.now().isoformat()
    }), 400


@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors"""
    return jsonify({
        'status': 'error',
        'message': 'Endpoint not found',
        'timestamp': datetime.now().isoformat()
    }), 404


@app.errorhandler(500)
def server_error(error):
    """Handle 500 errors"""
    return jsonify({
        'status': 'error',
        'message': 'Internal server error',
        'timestamp': datetime.now().isoformat()
    }), 500


if __name__ == '__main__':
    print("\n" + "="*60)
    print("🚀 Starting Voice to Text Flask Server")
    print("="*60)
    print("📍 Open http://localhost:5000 in your browser")
    print("🌐 Server: http://0.0.0.0:5000")
    print("🤖 ML Model: Ready for training")
    print("\n📊 API Endpoints:")
    print("   - GET  /api/health                 - Health check")
    print("   - GET  /api/model/stats            - Get model statistics")
    print("   - POST /api/model/correct          - Apply ML corrections")
    print("   - POST /api/model/train            - Train model")
    print("   - GET  /api/model/corrections      - Get learned corrections")
    print("   - GET  /api/model/export           - Export training data")
    print("   - POST /api/model/import           - Import training data")
    print("   - POST /api/model/reset            - Reset model")
    print("="*60 + "\n")
    
    # Run HTTP only for now (debug issue with HTTPS threading)
    app.run(
        debug=True,
        host='0.0.0.0',
        port=5000,
        use_reloader=True
    )
