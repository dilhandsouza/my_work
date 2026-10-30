// Initialize Speech Recognition API
const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

if (!SpeechRecognition) {
    alert('Speech Recognition API is not supported in your browser. Please use Chrome, Edge, or Safari.');
}

const recognition = new SpeechRecognition();
recognition.continuous = true;
recognition.interimResults = true;
recognition.lang = 'en-US';

// ============ ML Model Manager ============
class VoiceToTextModel {
    constructor() {
        this.trainingData = [];
        this.model = null;
        this.correctionDictionary = {};
        this.loadTrainingData();
        this.buildModel();
    }

    // Load training data from localStorage
    loadTrainingData() {
        const stored = localStorage.getItem('voiceToTextTraining');
        if (stored) {
            this.trainingData = JSON.parse(stored);
            this.buildCorrectionDictionary();
        }
    }

    // Save training data to localStorage
    saveTrainingData() {
        localStorage.setItem('voiceToTextTraining', JSON.stringify(this.trainingData));
    }

    // Build correction dictionary from training data
    buildCorrectionDictionary() {
        this.correctionDictionary = {};
        this.trainingData.forEach(pair => {
            if (pair.original && pair.corrected) {
                // Store character-level corrections
                const originalWords = pair.original.split(/\s+/);
                const correctedWords = pair.corrected.split(/\s+/);
                
                for (let i = 0; i < Math.min(originalWords.length, correctedWords.length); i++) {
                    if (originalWords[i] !== correctedWords[i]) {
                        const key = originalWords[i].toLowerCase();
                        if (!this.correctionDictionary[key]) {
                            this.correctionDictionary[key] = {};
                        }
                        const correctedWord = correctedWords[i];
                        this.correctionDictionary[key][correctedWord] = 
                            (this.correctionDictionary[key][correctedWord] || 0) + 1;
                    }
                }
            }
        });
    }

    // Build a simple neural network model
    buildModel() {
        if (this.model) {
            this.model.dispose();
        }

        this.model = tf.sequential({
            layers: [
                tf.layers.dense({ 
                    inputShape: [128], 
                    units: 64, 
                    activation: 'relu' 
                }),
                tf.layers.dropout({ rate: 0.2 }),
                tf.layers.dense({ 
                    units: 32, 
                    activation: 'relu' 
                }),
                tf.layers.dropout({ rate: 0.2 }),
                tf.layers.dense({ 
                    units: 16, 
                    activation: 'relu' 
                }),
                tf.layers.dense({ 
                    units: 1, 
                    activation: 'sigmoid' 
                })
            ]
        });

        this.model.compile({
            optimizer: tf.train.adam(0.01),
            loss: 'binaryCrossentropy',
            metrics: ['accuracy']
        });
    }

    // Add training sample when user corrects text
    addTrainingSample(original, corrected) {
        if (original.trim() !== corrected.trim()) {
            this.trainingData.push({
                original: original.trim(),
                corrected: corrected.trim(),
                timestamp: new Date().toISOString()
            });
            this.saveTrainingData();
            this.buildCorrectionDictionary();
            this.updateModelAccuracy();
            return true;
        }
        return false;
    }

    // Apply learned corrections to new text
    applyCorrections(text) {
        if (Object.keys(this.correctionDictionary).length === 0) {
            return text;
        }

        const words = text.split(/\s+/);
        return words.map(word => {
            const lowerWord = word.toLowerCase();
            
            // Check if we have a correction for this word
            if (this.correctionDictionary[lowerWord]) {
                // Find the most common correction
                const corrections = this.correctionDictionary[lowerWord];
                const bestCorrection = Object.keys(corrections).reduce((a, b) => 
                    corrections[a] > corrections[b] ? a : b
                );
                
                // Preserve original casing
                if (word[0] === word[0].toUpperCase()) {
                    return bestCorrection.charAt(0).toUpperCase() + bestCorrection.slice(1);
                }
                return bestCorrection;
            }
            
            return word;
        }).join(' ');
    }

    // Generate synthetic training data for the neural network
    async train() {
        if (this.trainingData.length < 2) {
            console.log('Not enough training data');
            return 0;
        }

        try {
            // Create training data for the model
            const inputs = [];
            const labels = [];

            this.trainingData.forEach(pair => {
                // Convert text to character codes (simple encoding)
                const textVector = this.encodeText(pair.original);
                const hasCorrection = pair.original !== pair.corrected ? 1 : 0;
                inputs.push(textVector);
                labels.push([hasCorrection]);
            });

            // Pad or truncate to 128 features
            const maxLength = 128;
            const paddedInputs = inputs.map(input => {
                const padded = new Array(maxLength).fill(0);
                for (let i = 0; i < Math.min(input.length, maxLength); i++) {
                    padded[i] = input[i] / 255; // Normalize
                }
                return padded;
            });

            const xs = tf.tensor2d(paddedInputs);
            const ys = tf.tensor2d(labels);

            await this.model.fit(xs, ys, {
                epochs: 5,
                batchSize: 1,
                verbose: 0
            });

            xs.dispose();
            ys.dispose();

            return this.calculateAccuracy();
        } catch (error) {
            console.error('Training error:', error);
            return 0;
        }
    }

    // Simple text encoding
    encodeText(text) {
        const codes = [];
        for (let i = 0; i < text.length; i++) {
            codes.push(text.charCodeAt(i));
        }
        return codes;
    }

    // Calculate model accuracy on training data
    calculateAccuracy() {
        if (this.trainingData.length === 0) return 0;
        
        let correctCount = 0;
        this.trainingData.forEach(pair => {
            const original = pair.original;
            const corrected = pair.corrected;
            const predicted = this.applyCorrections(original);
            
            const similarity = this.calculateSimilarity(predicted, corrected);
            if (similarity > 0.7) {
                correctCount++;
            }
        });
        
        return Math.round((correctCount / this.trainingData.length) * 100);
    }

    // Calculate similarity between two strings
    calculateSimilarity(str1, str2) {
        const longer = str1.length > str2.length ? str1 : str2;
        const shorter = str1.length > str2.length ? str2 : str1;
        
        if (longer.length === 0) return 1.0;
        
        const editDistance = this.levenshteinDistance(longer, shorter);
        return (longer.length - editDistance) / longer.length;
    }

    // Levenshtein distance algorithm
    levenshteinDistance(s1, s2) {
        const costs = [];
        for (let i = 0; i <= s1.length; i++) {
            let lastValue = i;
            for (let j = 0; j <= s2.length; j++) {
                if (i === 0) {
                    costs[j] = j;
                } else if (j > 0) {
                    let newValue = costs[j - 1];
                    if (s1.charAt(i - 1) !== s2.charAt(j - 1)) {
                        newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
                    }
                    costs[j - 1] = lastValue;
                    lastValue = newValue;
                }
            }
            if (i > 0) costs[s2.length] = lastValue;
        }
        return costs[s2.length];
    }

    // Reset model
    reset() {
        this.trainingData = [];
        this.correctionDictionary = {};
        localStorage.removeItem('voiceToTextTraining');
        this.buildModel();
    }

    // Export training data
    exportData() {
        return JSON.stringify(this.trainingData, null, 2);
    }

    // Import training data
    importData(jsonString) {
        try {
            const data = JSON.parse(jsonString);
            if (Array.isArray(data)) {
                this.trainingData = data;
                this.saveTrainingData();
                this.buildCorrectionDictionary();
                this.updateModelAccuracy();
                return true;
            }
        } catch (error) {
            console.error('Import error:', error);
        }
        return false;
    }

    // Update UI with model stats
    updateModelStats() {
        const status = this.trainingData.length === 0 ? 'Untrained' : 'Trained';
        const accuracy = this.calculateAccuracy();
        
        document.getElementById('modelStatus').textContent = status;
        document.getElementById('trainingSamples').textContent = this.trainingData.length;
        document.getElementById('modelAccuracy').textContent = accuracy + '%';
    }

    updateModelAccuracy() {
        const accuracy = this.calculateAccuracy();
        document.getElementById('modelAccuracy').textContent = accuracy + '%';
    }

    getTrainingCount() {
        return this.trainingData.length;
    }
}

// ============ Initialize Components ============
const mlModel = new VoiceToTextModel();

// DOM Elements
const recordBtn = document.getElementById('recordBtn');
const stopBtn = document.getElementById('stopBtn');
const clearBtn = document.getElementById('clearBtn');
const copyBtn = document.getElementById('copyBtn');
const downloadBtn = document.getElementById('downloadBtn');
const trainBtn = document.getElementById('trainBtn');
const dismissBtn = document.getElementById('dismissBtn');
const exportBtn = document.getElementById('exportBtn');
const importBtn = document.getElementById('importBtn');
const resetModelBtn = document.getElementById('resetModelBtn');
const textOutput = document.getElementById('textOutput');
const status = document.getElementById('status');
const recordingIndicator = document.getElementById('recordingIndicator');
const charCount = document.getElementById('charCount');
const wordCount = document.getElementById('wordCount');
const correctionSection = document.getElementById('correctionSection');
const correctionText = document.getElementById('correctionText');

let isRecording = false;
let finalTranscript = '';
let interimTranscript = '';
let lastOriginalText = '';

// Event Listeners
recordBtn.addEventListener('click', startRecording);
stopBtn.addEventListener('click', stopRecording);
clearBtn.addEventListener('click', clearText);
copyBtn.addEventListener('click', copyToClipboard);
downloadBtn.addEventListener('click', downloadAsText);
trainBtn.addEventListener('click', saveCorrection);
dismissBtn.addEventListener('click', dismissCorrection);
exportBtn.addEventListener('click', exportTrainingData);
importBtn.addEventListener('click', importTrainingData);
resetModelBtn.addEventListener('click', resetModel);
textOutput.addEventListener('input', handleTextChange);

// Speech Recognition Events
recognition.onstart = () => {
    isRecording = true;
    recordBtn.disabled = true;
    stopBtn.disabled = false;
    recordingIndicator.classList.remove('hidden');
    status.textContent = '🔴 Recording... Speak now!';
    status.style.borderLeftColor = '#f56565';
};

recognition.onresult = (event) => {
    interimTranscript = '';

    for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;

        if (event.results[i].isFinal) {
            finalTranscript += transcript + ' ';
        } else {
            interimTranscript += transcript;
        }
    }

    // Apply learned corrections to interim results
    const tempText = finalTranscript + interimTranscript;
    const correctedText = mlModel.applyCorrections(tempText);
    
    textOutput.value = correctedText;
    textOutput.scrollTop = textOutput.scrollHeight;
};

recognition.onerror = (event) => {
    status.textContent = `⚠️ Error: ${event.error}`;
    status.style.borderLeftColor = '#f56565';
    console.error('Speech recognition error', event.error);
};

recognition.onend = () => {
    isRecording = false;
    recordBtn.disabled = false;
    stopBtn.disabled = true;
    recordingIndicator.classList.add('hidden');
    
    // Apply learned corrections to final text
    const correctedFinal = mlModel.applyCorrections(finalTranscript);
    textOutput.value = correctedFinal;
    lastOriginalText = finalTranscript.trim();
    
    updateStats();
    
    if (correctedFinal.trim()) {
        status.textContent = '✅ Recording finished! Edit if needed, then save corrections.';
        status.style.borderLeftColor = '#48bb78';
    } else {
        status.textContent = 'No speech detected. Try again!';
        status.style.borderLeftColor = '#ed8936';
    }
};

// Functions
function startRecording() {
    if (!isRecording) {
        finalTranscript = '';
        interimTranscript = '';
        textOutput.value = '';
        lastOriginalText = '';
        recognition.start();
    }
}

function stopRecording() {
    if (isRecording) {
        recognition.stop();
    }
}

function clearText() {
    textOutput.value = '';
    finalTranscript = '';
    interimTranscript = '';
    lastOriginalText = '';
    updateStats();
    dismissCorrection();
    status.textContent = 'Text cleared. Ready to record';
    status.style.borderLeftColor = '#667eea';
}

function handleTextChange() {
    updateStats();
    
    const currentText = textOutput.value;
    const originalText = lastOriginalText || finalTranscript.trim() + interimTranscript.trim();
    
    if (currentText.trim() !== originalText.trim() && currentText.trim() && originalText) {
        showCorrectionPrompt(originalText, currentText);
    } else {
        dismissCorrection();
    }
}

function showCorrectionPrompt(original, corrected) {
    correctionText.textContent = `We detected a change. Would you like to teach the model this correction? Original: "${original}" → Corrected: "${corrected}"`;
    correctionSection.classList.add('active');
}

function dismissCorrection() {
    correctionSection.classList.remove('active');
}

function saveCorrection() {
    const currentText = textOutput.value.trim();
    const originalText = lastOriginalText || finalTranscript.trim() + interimTranscript.trim();
    
    if (mlModel.addTrainingSample(originalText, currentText)) {
        dismissCorrection();
        showSuccessMessage('🤖 Model learned this correction!');
        status.textContent = '✅ Correction saved! Model improved.';
        status.style.borderLeftColor = '#48bb78';
        mlModel.updateModelStats();
    }
}

function copyToClipboard() {
    const text = textOutput.value;
    
    if (!text.trim()) {
        status.textContent = '⚠️ Nothing to copy!';
        status.style.borderLeftColor = '#ed8936';
        return;
    }

    navigator.clipboard.writeText(text).then(() => {
        showSuccessMessage('📋 Copied to clipboard!');
        status.textContent = '✅ Text copied to clipboard!';
        status.style.borderLeftColor = '#48bb78';
    }).catch(() => {
        status.textContent = '❌ Failed to copy';
        status.style.borderLeftColor = '#f56565';
    });
}

function downloadAsText() {
    const text = textOutput.value;
    
    if (!text.trim()) {
        status.textContent = '⚠️ Nothing to download!';
        status.style.borderLeftColor = '#ed8936';
        return;
    }

    const element = document.createElement('a');
    const file = new Blob([text], { type: 'text/plain' });
    element.href = URL.createObjectURL(file);
    element.download = `voice-transcription-${new Date().getTime()}.txt`;
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
    
    showSuccessMessage('💾 File downloaded!');
    status.textContent = '✅ File downloaded successfully!';
    status.style.borderLeftColor = '#48bb78';
}

function exportTrainingData() {
    const data = mlModel.exportData();
    const element = document.createElement('a');
    const file = new Blob([data], { type: 'application/json' });
    element.href = URL.createObjectURL(file);
    element.download = `voice-model-training-${new Date().getTime()}.json`;
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
    
    showSuccessMessage('💾 Training data exported!');
    status.textContent = '✅ Training data exported!';
    status.style.borderLeftColor = '#48bb78';
}

function importTrainingData() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json';
    
    input.onchange = (e) => {
        const file = e.target.files[0];
        const reader = new FileReader();
        
        reader.onload = (event) => {
            if (mlModel.importData(event.target.result)) {
                showSuccessMessage('✅ Training data imported!');
                status.textContent = '✅ Training data imported successfully!';
                status.style.borderLeftColor = '#48bb78';
                mlModel.updateModelStats();
            } else {
                status.textContent = '❌ Failed to import data';
                status.style.borderLeftColor = '#f56565';
            }
        };
        
        reader.readAsText(file);
    };
    
    input.click();
}

function resetModel() {
    if (confirm('Are you sure? This will delete all training data.')) {
        mlModel.reset();
        mlModel.updateModelStats();
        showSuccessMessage('🔄 Model reset!');
        status.textContent = '✅ Model has been reset';
        status.style.borderLeftColor = '#48bb78';
    }
}

function updateStats() {
    const text = textOutput.value;
    charCount.textContent = text.length;
    
    const words = text.trim().split(/\s+/).filter(word => word.length > 0);
    wordCount.textContent = text.trim() === '' ? 0 : words.length;
}

function showSuccessMessage(message) {
    const messageDiv = document.createElement('div');
    messageDiv.className = 'success-message';
    messageDiv.textContent = message;
    
    const controlsSection = document.querySelector('.controls-section');
    controlsSection.insertBefore(messageDiv, controlsSection.firstChild);
    
    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}

// Initialize
updateStats();
mlModel.updateModelStats();
console.log('Voice to Text Converter with ML Model loaded successfully!');
