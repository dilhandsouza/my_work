// Flask Voice to Text Frontend
// Uses Python ML backend via REST API

const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;

if (!SpeechRecognition) {
    alert('Speech Recognition API is not supported in your browser. Please use Chrome, Edge, or Safari.');
}

const recognition = new SpeechRecognition();
recognition.continuous = true;
recognition.interimResults = true;
recognition.lang = 'en-US';
recognition.maxAlternatives = 1;

// Add a longer timeout before giving up - 30 seconds
recognition.timeout = 30000;

// DOM Elements (will be initialized in setup function)
let recordBtn, stopBtn, clearBtn, copyBtn, downloadBtn, trainBtn, dismissBtn;
let exportBtn, importBtn, resetModelBtn, textOutput, status, recordingIndicator;
let charCount, wordCount, correctionSection, correctionText;
let modelStatus, trainingSamples, modelAccuracy, importFile, languageDisplay;
let languageSelectorBtn, languageSelectorModal, modalCloseBtn, languageGrid;

let isRecording = false;
let finalTranscript = '';
let interimTranscript = '';
let lastOriginalText = '';
let noSpeechRetryCount = 0;
let recordingStartTime = 0;
let currentLanguage = 'en-US';
let currentLanguageName = 'English';

// Language mapping for voice commands
const languageMap = {
    'kannada': { code: 'kn-IN', name: 'Kannada' },
    'hindi': { code: 'hi-IN', name: 'Hindi' },
    'tamil': { code: 'ta-IN', name: 'Tamil' },
    'telugu': { code: 'te-IN', name: 'Telugu' },
    'english': { code: 'en-US', name: 'English' },
    'spanish': { code: 'es-ES', name: 'Spanish' },
    'french': { code: 'fr-FR', name: 'French' },
    'german': { code: 'de-DE', name: 'German' },
    'portuguese': { code: 'pt-BR', name: 'Portuguese' },
    'mandarin': { code: 'zh-CN', name: 'Mandarin' },
    'japanese': { code: 'ja-JP', name: 'Japanese' }
};

// Initialize DOM elements and event listeners
function setupUI() {
    recordBtn = document.getElementById('recordBtn');
    stopBtn = document.getElementById('stopBtn');
    clearBtn = document.getElementById('clearBtn');
    copyBtn = document.getElementById('copyBtn');
    downloadBtn = document.getElementById('downloadBtn');
    trainBtn = document.getElementById('trainBtn');
    dismissBtn = document.getElementById('dismissBtn');
    exportBtn = document.getElementById('exportBtn');
    importBtn = document.getElementById('importBtn');
    resetModelBtn = document.getElementById('resetModelBtn');
    textOutput = document.getElementById('textOutput');
    status = document.getElementById('status');
    recordingIndicator = document.getElementById('recordingIndicator');
    charCount = document.getElementById('charCount');
    wordCount = document.getElementById('wordCount');
    correctionSection = document.getElementById('correctionSection');
    correctionText = document.getElementById('correctionText');
    modelStatus = document.getElementById('modelStatus');
    trainingSamples = document.getElementById('trainingSamples');
    modelAccuracy = document.getElementById('modelAccuracy');
    importFile = document.getElementById('importFile');
    languageDisplay = document.getElementById('languageDisplay');
    languageSelectorBtn = document.getElementById('languageSelectorBtn');
    languageSelectorModal = document.getElementById('languageSelectorModal');
    modalCloseBtn = document.getElementById('modalCloseBtn');
    languageGrid = document.getElementById('languageGrid');

    // Verify all elements exist
    const elements = [recordBtn, stopBtn, clearBtn, copyBtn, downloadBtn, trainBtn, dismissBtn,
                     exportBtn, importBtn, resetModelBtn, textOutput, status, recordingIndicator,
                     charCount, wordCount, correctionSection, correctionText, modelStatus,
                     trainingSamples, modelAccuracy, importFile, languageDisplay, languageSelectorBtn,
                     languageSelectorModal, modalCloseBtn, languageGrid];
    
    const missing = elements.filter(el => !el);
    if (missing.length > 0) {
        console.error('Missing DOM elements:', missing.length);
        alert('Error: Some UI elements could not be found. Please refresh the page.');
        return false;
    }

    // Event Listeners
    recordBtn.addEventListener('click', startRecording);
    stopBtn.addEventListener('click', stopRecording);
    clearBtn.addEventListener('click', clearText);
    copyBtn.addEventListener('click', copyToClipboard);
    downloadBtn.addEventListener('click', downloadAsText);
    trainBtn.addEventListener('click', saveCorrection);
    dismissBtn.addEventListener('click', dismissCorrection);
    exportBtn.addEventListener('click', exportTrainingData);
    importBtn.addEventListener('click', triggerImport);
    resetModelBtn.addEventListener('click', resetModel);
    textOutput.addEventListener('input', handleTextChange);
    importFile.addEventListener('change', importTrainingData);
    
    // Language selector listener - triggers voice command listening
    languageSelectorBtn.addEventListener('click', openLanguageSelector);
    
    // Modal listeners (kept for fallback)
    modalCloseBtn.addEventListener('click', closeLanguageSelector);
    languageSelectorModal.addEventListener('click', (e) => {
        // Close modal when clicking outside the content
        if (e.target === languageSelectorModal) {
            closeLanguageSelector();
        }
    });
    
    console.log('✅ All UI elements loaded and event listeners attached');
    return true;
}

// Language Detection Helper Functions
function isLanguageCommand(text) {
    const lowerText = text.toLowerCase();
    return lowerText.includes('start writing in') || 
           lowerText.includes('switch to') || 
           lowerText.includes('change to') ||
           lowerText.includes('write in') ||
           lowerText.includes('change language to') ||
           lowerText.includes('switch language to');
}

function checkForLanguageCommand(transcript) {
    const lowerTranscript = transcript.toLowerCase();
    
    // Check for language change patterns
    for (const [langName, langInfo] of Object.entries(languageMap)) {
        // Pattern: "start writing in kannada", "switch to kannada", "change to kannada", etc.
        const patterns = [
            `start writing in ${langName}`,
            `switch to ${langName}`,
            `change to ${langName}`,
            `write in ${langName}`,
            `change language to ${langName}`,
            `switch language to ${langName}`
        ];
        
        for (const pattern of patterns) {
            if (lowerTranscript.includes(pattern)) {
                console.log(`✅ Language command detected: "${pattern}"`);
                changeLanguage(langInfo.code, langInfo.name);
                return true;
            }
        }
    }
    return false;
}

function changeLanguage(langCode, langName) {
    console.log(`🌐 Changing language to ${langName} (${langCode})`);
    
    currentLanguage = langCode;
    currentLanguageName = langName;
    recognition.lang = langCode;
    
    // Update UI
    languageDisplay.textContent = langName;
    
    // Update language grid active state if modal is open
    if (!languageSelectorModal.classList.contains('hidden')) {
        updateLanguageGridActiveState();
    }
    
    // Clear current text to start fresh in new language
    finalTranscript = '';
    interimTranscript = '';
    textOutput.value = '';
    lastOriginalText = '';
    
    // Show status message
    status.textContent = `🌐 Language changed to ${langName}! Starting fresh...`;
    status.style.borderLeftColor = '#38b2ac';
    
    // Always stop current recognition (if running) and restart with new language
    console.log(`Current isRecording state: ${isRecording}`);
    
    try {
        // If currently recording, stop it first
        if (isRecording) {
            recognition.stop();
            console.log('Stopped current recording');
        } else {
            // Even if not recording, try to abort to reset state
            try {
                recognition.abort();
                console.log('Aborted recognition to reset state');
            } catch (e) {
                // Ignore if not running
            }
        }
        
        // Restart recognition with new language after a short delay
        setTimeout(() => {
            try {
                recognition.start();
                console.log(`✅ Recognition restarted with language: ${langName}`);
                isRecording = true;
                recordBtn.disabled = true;
                stopBtn.disabled = false;
                recordingIndicator.classList.remove('hidden');
                status.textContent = `🎤 Now recording in ${langName}! Speak now.`;
                status.style.borderLeftColor = '#4299e1';
            } catch (error) {
                console.error('Error restarting recognition:', error);
                status.textContent = `✅ Language changed to ${langName}! Click "Start Recording" to begin.`;
                status.style.borderLeftColor = '#48bb78';
            }
        }, 300);
    } catch (error) {
        console.error('Error during language change:', error);
        status.textContent = `✅ Language changed to ${langName}! Click "Start Recording" to begin.`;
        status.style.borderLeftColor = '#48bb78';
    }
}

// Language Selector Functions
function initializeLanguageGrid() {
    // Clear existing buttons
    languageGrid.innerHTML = '';
    
    // Create emoji map for languages
    const languageEmojis = {
        'Kannada': '🇮🇳',
        'Hindi': '🇮🇳',
        'Tamil': '🇮🇳',
        'Telugu': '🇮🇳',
        'English': '🇬🇧',
        'Spanish': '🇪🇸',
        'French': '🇫🇷',
        'German': '🇩🇪',
        'Portuguese': '🇧🇷',
        'Mandarin': '🇨🇳',
        'Japanese': '🇯🇵'
    };
    
    // Create button for each language
    for (const [langName, langInfo] of Object.entries(languageMap)) {
        const btn = document.createElement('button');
        btn.className = 'language-btn';
        btn.dataset.langCode = langInfo.code;
        btn.dataset.langName = langInfo.name;
        
        if (currentLanguage === langInfo.code) {
            btn.classList.add('active');
        }
        
        const emoji = languageEmojis[langInfo.name] || '🌐';
        btn.innerHTML = `
            <span class="language-icon">${emoji}</span>
            <span class="language-name">${langInfo.name}</span>
        `;
        
        btn.addEventListener('click', () => {
            changeLanguage(langInfo.code, langInfo.name);
            closeLanguageSelector();
        });
        
        languageGrid.appendChild(btn);
    }
}

function openLanguageSelector() {
    console.log('🌍 Opening voice language selector');
    status.textContent = '🎤 Listening for language command... Say a language name (Kannada, Hindi, etc.)';
    status.style.borderLeftColor = '#38b2ac';
    
    // Start listening for language commands
    listenForLanguageCommand();
}

function closeLanguageSelector() {
    console.log('🌍 Closing language selector');
    languageSelectorModal.classList.add('hidden');
}

function listenForLanguageCommand() {
    console.log('🎤 Starting to listen for language command');
    
    // Create a temporary recognition instance for listening to language commands
    const langRecognition = new SpeechRecognition();
    langRecognition.continuous = false;
    langRecognition.interimResults = true;
    langRecognition.lang = 'en-US'; // Always listen in English for language names
    langRecognition.maxAlternatives = 1;
    
    langRecognition.onstart = () => {
        status.textContent = '🎤 Listening for language command...';
        status.style.borderLeftColor = '#4299e1';
    };
    
    langRecognition.onresult = (event) => {
        let interimTranscript = '';
        let finalTranscript = '';
        
        for (let i = event.resultIndex; i < event.results.length; i++) {
            const transcript = event.results[i][0].transcript;
            
            if (event.results[i].isFinal) {
                finalTranscript += transcript + ' ';
            } else {
                interimTranscript += transcript;
            }
        }
        
        if (finalTranscript) {
            console.log(`📢 You said: ${finalTranscript}`);
            status.textContent = `📢 You said: "${finalTranscript.trim()}"... Detecting language...`;
            status.style.borderLeftColor = '#ed8936';
            
            // Check for language commands
            if (detectLanguageFromVoice(finalTranscript)) {
                langRecognition.stop();
            }
        } else if (interimTranscript) {
            status.textContent = `🎤 Listening... (you said: "${interimTranscript}")`;
        }
    };
    
    langRecognition.onerror = (event) => {
        console.error('Language command error:', event.error);
        status.textContent = `❌ Error listening for command: ${event.error}`;
        status.style.borderLeftColor = '#f56565';
    };
    
    langRecognition.onend = () => {
        console.log('Language command listening ended');
    };
    
    try {
        langRecognition.start();
    } catch (error) {
        console.error('Error starting language command recognition:', error);
        status.textContent = '❌ Error starting voice recognition';
        status.style.borderLeftColor = '#f56565';
    }
}

function detectLanguageFromVoice(transcript) {
    const lowerTranscript = transcript.toLowerCase();
    console.log(`🔍 Detecting language from: "${lowerTranscript}"`);
    
    // Check for each language
    for (const [langName, langInfo] of Object.entries(languageMap)) {
        // Check if the language name is mentioned
        if (lowerTranscript.includes(langName)) {
            console.log(`✅ Detected language: ${langInfo.name}`);
            status.textContent = `✅ Language command detected: ${langInfo.name}`;
            status.style.borderLeftColor = '#48bb78';
            
            // Change language after a short delay
            setTimeout(() => {
                changeLanguage(langInfo.code, langInfo.name);
            }, 500);
            return true;
        }
    }
    
    // Language not detected
    console.log('❌ No language detected in speech');
    status.textContent = '❌ Language not recognized. Try again: say a language name (Kannada, Hindi, Tamil, etc.)';
    status.style.borderLeftColor = '#f56565';
    
    // Restart listening for another attempt
    setTimeout(() => {
        listenForLanguageCommand();
    }, 1500);
    
    return false;
}

// Speech Recognition Events
recognition.onstart = () => {
    console.log('🎤 Recording started...');
    isRecording = true;
    recordingStartTime = Date.now();
    noSpeechRetryCount = 0;
    recordBtn.disabled = true;
    stopBtn.disabled = false;
    recordingIndicator.classList.remove('hidden');
    status.textContent = '🎤 Listening... Speak now!';
    status.style.borderLeftColor = '#4299e1';
};

recognition.onend = () => {
    console.log('🎤 Recording ended');
    isRecording = false;
    recordBtn.disabled = false;
    stopBtn.disabled = true;
    recordingIndicator.classList.add('hidden');
    
    textOutput.value = finalTranscript;
    lastOriginalText = finalTranscript.trim();
    
    updateStats();
    
    if (finalTranscript.trim()) {
        status.textContent = '✅ Recording finished! Edit if needed, then save corrections.';
        status.style.borderLeftColor = '#48bb78';
    } else {
        status.textContent = 'Ready to record again. Click "Start Recording" or speak a language command.';
        status.style.borderLeftColor = '#ed8936';
    }
};

recognition.onerror = (event) => {
    console.error('🔴 Speech Recognition Error:', event.error);
    
    let errorMessage = 'Error: ';
    let shouldRetry = false;
    
    switch(event.error) {
        case 'no-speech':
            errorMessage = '⏳ No speech detected. Trying again...';
            shouldRetry = true;
            noSpeechRetryCount++;
            console.log(`No speech detected. Retry attempt ${noSpeechRetryCount}`);
            break;
        case 'audio-capture':
            errorMessage += 'No microphone found. Check permissions.';
            isRecording = false;
            recordBtn.disabled = false;
            stopBtn.disabled = true;
            recordingIndicator.classList.add('hidden');
            break;
        case 'network':
            errorMessage += 'Network error. Check your connection.';
            isRecording = false;
            recordBtn.disabled = false;
            stopBtn.disabled = true;
            recordingIndicator.classList.add('hidden');
            break;
        case 'permission-denied':
            errorMessage += 'Microphone permission denied. Allow access in browser settings.';
            isRecording = false;
            recordBtn.disabled = false;
            stopBtn.disabled = true;
            recordingIndicator.classList.add('hidden');
            break;
        default:
            errorMessage += event.error;
            isRecording = false;
            recordBtn.disabled = false;
            stopBtn.disabled = true;
            recordingIndicator.classList.add('hidden');
    }
    
    status.textContent = errorMessage;
    status.style.borderLeftColor = errorMessage.includes('Trying again') ? '#ed8936' : '#f56565';
    
    // Auto-retry on no-speech error up to 3 times
    if (shouldRetry && noSpeechRetryCount < 3) {
        console.log('Restarting speech recognition...');
        setTimeout(() => {
            try {
                recognition.start();
            } catch (e) {
                console.log('Cannot restart immediately, will retry on next attempt');
            }
        }, 500);
    } else if (shouldRetry && noSpeechRetryCount >= 3) {
        // Give up after 3 attempts
        isRecording = false;
        recordBtn.disabled = false;
        stopBtn.disabled = true;
        recordingIndicator.classList.add('hidden');
        status.textContent = '❌ No speech detected after multiple attempts. Check your microphone.';
        status.style.borderLeftColor = '#f56565';
    }
};

recognition.onresult = async (event) => {
    // Reset interim transcript for this event
    interimTranscript = '';
    
    // Check if any speech was detected
    let speechDetected = false;
    let fullTranscript = '';

    for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcript = event.results[i][0].transcript;

        if (transcript.trim().length > 0) {
            speechDetected = true;
            // Reset retry count since we got speech
            noSpeechRetryCount = 0;
        }

        if (event.results[i].isFinal) {
            finalTranscript += transcript + ' ';
            fullTranscript = finalTranscript;
            status.textContent = '✅ Speech detected! Processing...';
            status.style.borderLeftColor = '#48bb78';
            
            // Check if this is a language change command
            checkForLanguageCommand(transcript);
        } else {
            interimTranscript += transcript;
            status.textContent = '🎤 Listening... (partial: ' + transcript.substring(0, 20) + ')';
            status.style.borderLeftColor = '#4299e1';
        }
    }

    const tempText = finalTranscript + interimTranscript;
    
    // Apply ML corrections via API (only if not a language command)
    if (!isLanguageCommand(tempText)) {
        try {
            if (tempText.trim().length > 0) {
                const response = await fetch('/api/model/correct', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ text: tempText })
                });

                const data = await response.json();
                if (data.status === 'success') {
                    textOutput.value = data.corrected;
                } else {
                    textOutput.value = tempText;
                }
            }
        } catch (error) {
            console.error('Correction error:', error);
            textOutput.value = tempText;
        }
    }

    textOutput.scrollTop = textOutput.scrollHeight;
    updateStats();
};

// Functions
function startRecording() {
    if (!isRecording) {
        console.log('🔴 Start recording button clicked');
        finalTranscript = '';
        interimTranscript = '';
        textOutput.value = '';
        lastOriginalText = '';
        noSpeechRetryCount = 0;
        recordingStartTime = Date.now();
        
        try {
            // If already running, stop first
            if (isRecording) {
                recognition.stop();
                console.log('Stopped previous recording');
            }
            
            status.textContent = '⏳ Initializing microphone...';
            status.style.borderLeftColor = '#ed8936';
            
            // Small delay to ensure previous instance stopped
            setTimeout(() => {
                recognition.start();
                console.log('✅ Recognition.start() called successfully');
            }, 100);
        } catch (error) {
            console.error('❌ Error starting recognition:', error);
            status.textContent = '❌ Error starting recording: ' + error.message;
            status.style.borderLeftColor = '#f56565';
            recordBtn.disabled = false;
            stopBtn.disabled = true;
        }
    } else {
        console.log('⚠️ Already recording');
    }
}

function stopRecording() {
    if (isRecording) {
        console.log('⏹️ Stopping recording...');
        recognition.stop();
        isRecording = false;
        recordBtn.disabled = false;
        stopBtn.disabled = true;
        recordingIndicator.classList.add('hidden');
        setTimeout(() => {
            // Force abort if still running
            try {
                recognition.abort();
            } catch (e) {
                // Already stopped, ignore
            }
        }, 500);
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

async function saveCorrection() {
    const currentText = textOutput.value.trim();
    const originalText = lastOriginalText || finalTranscript.trim() + interimTranscript.trim();
    
    try {
        const response = await fetch('/api/model/train', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                original: originalText,
                corrected: currentText
            })
        });

        const data = await response.json();
        
        if (data.status === 'success') {
            dismissCorrection();
            showSuccessMessage('🤖 Model learned this correction!');
            status.textContent = '✅ Correction saved! Model improved.';
            status.style.borderLeftColor = '#48bb78';
            updateModelStats();
        }
    } catch (error) {
        console.error('Training error:', error);
        status.textContent = '❌ Failed to save correction';
        status.style.borderLeftColor = '#f56565';
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

async function exportTrainingData() {
    try {
        window.location.href = '/api/model/export';
        showSuccessMessage('💾 Model data exported!');
    } catch (error) {
        status.textContent = '❌ Export failed';
        status.style.borderLeftColor = '#f56565';
    }
}

function triggerImport() {
    importFile.click();
}

async function importTrainingData(event) {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch('/api/model/import', {
            method: 'POST',
            body: formData
        });

        const data = await response.json();
        
        if (data.status === 'success') {
            showSuccessMessage('✅ Training data imported!');
            status.textContent = '✅ Training data imported successfully!';
            status.style.borderLeftColor = '#48bb78';
            updateModelStats();
        } else {
            status.textContent = '❌ Failed to import data';
            status.style.borderLeftColor = '#f56565';
        }
    } catch (error) {
        console.error('Import error:', error);
        status.textContent = '❌ Import failed';
        status.style.borderLeftColor = '#f56565';
    }

    // Reset file input
    event.target.value = '';
}

async function resetModel() {
    if (confirm('Are you sure? This will delete all training data.')) {
        try {
            const response = await fetch('/api/model/reset', {
                method: 'POST'
            });

            const data = await response.json();
            
            if (data.status === 'success') {
                showSuccessMessage('🔄 Model reset!');
                status.textContent = '✅ Model has been reset';
                status.style.borderLeftColor = '#48bb78';
                updateModelStats();
            }
        } catch (error) {
            status.textContent = '❌ Reset failed';
            status.style.borderLeftColor = '#f56565';
        }
    }
}

function updateStats() {
    const text = textOutput.value;
    charCount.textContent = text.length;
    
    const words = text.trim().split(/\s+/).filter(word => word.length > 0);
    wordCount.textContent = text.trim() === '' ? 0 : words.length;
}

async function updateModelStats() {
    try {
        const response = await fetch('/api/model/stats');
        const data = await response.json();
        
        if (data.status === 'success') {
            const stats = data.data;
            modelStatus.textContent = stats.status;
            trainingSamples.textContent = stats.training_samples;
            modelAccuracy.textContent = stats.accuracy + '%';
        }
    } catch (error) {
        console.error('Stats error:', error);
    }
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

// Initialize on load
window.addEventListener('load', () => {
    console.log('📄 DOM Content Loaded - Initializing app...');
    
    // Setup UI elements and event listeners
    if (!setupUI()) {
        console.error('❌ Failed to setup UI');
        return;
    }
    
    // Initialize language display
    if (languageDisplay) {
        languageDisplay.textContent = currentLanguageName;
    }
    
    // Initialize stats
    updateStats();
    updateModelStats();
    
    console.log('✅ RecognitionAPI capabilities:', {
        continuous: recognition.continuous,
        interimResults: recognition.interimResults,
        language: recognition.lang
    });
    
    console.log('🚀 Flask Voice to Text App loaded successfully!');
    console.log('📋 Supported languages:', Object.keys(languageMap).map(k => languageMap[k].name).join(', '));
    console.log('💡 Tip: Say "start writing in [language]" to switch languages');
});

// Also setup on DOMContentLoaded as backup
document.addEventListener('DOMContentLoaded', () => {
    console.log('🎯 DOMContentLoaded event fired');
    if (typeof setupUI === 'function') {
        setupUI();
    }
});
