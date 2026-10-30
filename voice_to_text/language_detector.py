"""
Language Detection & Grammar Correction Application
Detects language from text and provides grammar corrections
"""

from langdetect import detect, detect_langs, LangDetectException
import language_tool_python
import sys
from datetime import datetime

# Initialize grammar tool (supports 25+ languages)
try:
    grammar_tool = language_tool_python.LanguageTool('en-US')
except:
    print("⚠️  LanguageTool initialization warning...")
    grammar_tool = None

# Language code to language name mapping
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

def detect_language(text):
    """
    Detect the language of the given text
    Returns: (language_code, language_name, confidence)
    """
    try:
        if not text.strip():
            return None, 'Empty text', 0.0
        
        # Detect language
        lang_code = detect(text)
        
        # Get confidence scores
        probabilities = detect_langs(text)
        confidence = max([p.prob for p in probabilities]) if probabilities else 0.0
        
        # Get language name
        lang_name = LANGUAGE_NAMES.get(lang_code, lang_code.upper())
        
        return lang_code, lang_name, confidence
    except LangDetectException as e:
        return None, 'Unknown', 0.0

def correct_grammar(text, language_code='en-US'):
    """
    Correct grammar and spelling in the text
    Returns: corrected_text
    """
    if not grammar_tool:
        return text
    
    try:
        # Map language codes to LanguageTool format
        lang_map = {
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
        }
        
        lang = lang_map.get(language_code, 'en-US')
        
        # Create tool for the detected language
        tool = language_tool_python.LanguageTool(lang)
        
        # Get matches
        matches = tool.check(text)
        
        # Apply corrections
        corrected = text
        for match in reversed(matches):
            if match.replacements:
                # Use the first suggestion
                corrected = corrected[:match.offset] + match.replacements[0] + corrected[match.offset + match.length:]
        
        return corrected
    except Exception as e:
        print(f"⚠️  Grammar correction error: {e}")
        return text

def analyze_text(text):
    """
    Full analysis: detect language and correct grammar
    Returns: dictionary with results
    """
    # Detect language
    lang_code, lang_name, confidence = detect_language(text)
    
    # Correct grammar
    corrected_text = correct_grammar(text, lang_code) if lang_code else text
    
    # Calculate difference
    has_errors = text != corrected_text
    
    result = {
        'original_text': text,
        'corrected_text': corrected_text,
        'detected_language': lang_name,
        'language_code': lang_code,
        'confidence': f'{confidence * 100:.1f}%',
        'has_errors': has_errors,
        'timestamp': datetime.now().isoformat()
    }
    
    return result

def display_result(result):
    """
    Display the analysis result in a formatted way
    """
    print("\n" + "="*70)
    print("🌐 LANGUAGE DETECTION & GRAMMAR CORRECTION")
    print("="*70)
    
    print(f"\n📍 Detected Language: {result['detected_language']}")
    print(f"   Code: {result['language_code']}")
    print(f"   Confidence: {result['confidence']}")
    
    print(f"\n📝 Original Text:")
    print(f"   {result['original_text']}")
    
    if result['has_errors']:
        print(f"\n✅ Corrected Text:")
        print(f"   {result['corrected_text']}")
        print(f"\n🔧 Status: Corrections Applied")
    else:
        print(f"\n✅ No grammar errors detected!")
    
    print("\n" + "="*70 + "\n")

def main():
    """
    Main application function
    """
    print("\n" + "="*70)
    print("🎯 LANGUAGE DETECTION & GRAMMAR CORRECTION TOOL")
    print("="*70)
    print("\nSupported: 25+ languages including English, Spanish, French,")
    print("German, Portuguese, Russian, Chinese, Japanese, Hindi, and more!")
    print("\nCommands:")
    print("  - Type text to analyze")
    print("  - 'file <path>' to analyze a file")
    print("  - 'exit' to quit")
    print("="*70 + "\n")
    
    while True:
        try:
            user_input = input("📝 Enter text (or command): ").strip()
            
            if not user_input:
                print("⚠️  Please enter some text to analyze.\n")
                continue
            
            if user_input.lower() == 'exit':
                print("\n👋 Thank you for using the Language Detection Tool!\n")
                break
            
            if user_input.lower().startswith('file '):
                # Read from file
                file_path = user_input[5:].strip()
                try:
                    with open(file_path, 'r', encoding='utf-8') as f:
                        text = f.read().strip()
                    if not text:
                        print("❌ File is empty.\n")
                        continue
                except FileNotFoundError:
                    print(f"❌ File not found: {file_path}\n")
                    continue
            else:
                text = user_input
            
            # Analyze
            print("\n🔄 Analyzing...\n")
            result = analyze_text(text)
            
            # Display result
            display_result(result)
            
        except KeyboardInterrupt:
            print("\n\n👋 Interrupted! Goodbye!\n")
            break
        except Exception as e:
            print(f"\n❌ Error: {e}\n")

if __name__ == '__main__':
    if len(sys.argv) > 1:
        # Command line mode
        text = ' '.join(sys.argv[1:])
        result = analyze_text(text)
        display_result(result)
    else:
        # Interactive mode
        main()
