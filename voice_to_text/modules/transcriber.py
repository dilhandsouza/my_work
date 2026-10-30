"""
Audio Transcription Module
Handles transcribing audio files to text.
"""

import speech_recognition as sr
import os


class AudioTranscriber:
    """Class for transcribing audio files to text."""
    
    def __init__(self):
        """Initialize the audio transcriber."""
        self.recognizer = sr.Recognizer()
    
    def transcribe(self, audio_file):
        """
        Transcribe an audio file to text.
        
        Args:
            audio_file (str): Path to the audio file
        
        Returns:
            str: Transcribed text, or None if transcription failed
        """
        if not os.path.exists(audio_file):
            print(f"Error: File '{audio_file}' not found.")
            return None
        
        try:
            with sr.AudioFile(audio_file) as source:
                audio = self.recognizer.record(source)
            
            text = self.recognizer.recognize_google(audio)
            return text
        
        except sr.UnknownValueError:
            print("Could not understand audio.")
            return None
        except sr.RequestError as e:
            print(f"Error with speech recognition service: {e}")
            return None
        except Exception as e:
            print(f"Error: {e}")
            return None
