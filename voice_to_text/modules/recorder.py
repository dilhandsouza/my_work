"""
Audio Recording Module
Handles recording audio from the microphone.
"""

import speech_recognition as sr
from datetime import datetime


class AudioRecorder:
    """Class for recording audio from microphone."""
    
    def __init__(self):
        """Initialize the audio recorder."""
        self.recognizer = sr.Recognizer()
        self.microphone = sr.Microphone()
    
    def record(self, duration=None):
        """
        Record audio from the microphone.
        
        Args:
            duration: Maximum recording duration in seconds (None for unlimited)
        
        Returns:
            str: Path to the saved audio file, or None if recording failed
        """
        try:
            with self.microphone as source:
                self.recognizer.adjust_for_ambient_noise(source, duration=1)
                print("Listening...")
                
                audio = self.recognizer.listen(source, timeout=duration)
            
            # Save audio to file
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            audio_file = f"recording_{timestamp}.wav"
            
            with open(audio_file, "wb") as f:
                f.write(audio.get_wav_data())
            
            return audio_file
        
        except sr.UnknownValueError:
            print("Could not understand audio.")
            return None
        except sr.RequestError as e:
            print(f"Error: {e}")
            return None
