#!/usr/bin/env python3
"""
Voice to Text Converter with Machine Learning
Main application entry point with integrated ML model
"""

import sys
import os
from modules.recorder import AudioRecorder
from modules.transcriber import AudioTranscriber
from model import VoiceToTextModel


class VoiceToTextApp:
    """Main application with ML model integration"""
    
    def __init__(self):
        """Initialize the application"""
        self.recorder = AudioRecorder()
        self.transcriber = AudioTranscriber()
        self.ml_model = VoiceToTextModel()
    
    def display_banner(self) -> None:
        """Display application banner"""
        print("\n" + "=" * 60)
        print("🎤 Voice to Text Converter with ML Learning")
        print("=" * 60)
        print("The model learns from your corrections and improves!")
        print("=" * 60 + "\n")
    
    def display_menu(self) -> None:
        """Display main menu options"""
        print("\nOptions:")
        print("1. 🎙️  Record and transcribe audio")
        print("2. 📁 Transcribe existing audio file")
        print("3. 📊 View model statistics")
        print("4. 📚 Show learned corrections")
        print("5. 💾 Export training data")
        print("6. 📂 Import training data")
        print("7. 🔄 Reset model")
        print("8. ❌ Exit")
    
    def record_and_transcribe(self) -> None:
        """Record audio and apply ML corrections"""
        try:
            print("\n🎤 Recording audio... (press Ctrl+C to stop)")
            audio_file = self.recorder.record()
            
            if not audio_file:
                print("❌ Recording failed")
                return
            
            print(f"\n✅ Audio saved: {audio_file}")
            print("🔄 Transcribing...")
            
            original_text = self.transcriber.transcribe(audio_file)
            
            if not original_text:
                print("❌ Transcription failed")
                return
            
            # Apply ML corrections
            corrected_text = self.ml_model.apply_corrections(original_text)
            
            print("\n" + "=" * 60)
            print("📝 Original Transcription:")
            print("-" * 60)
            print(original_text)
            print("-" * 60)
            
            if original_text != corrected_text:
                print("\n🤖 ML Corrected Text:")
                print("-" * 60)
                print(corrected_text)
                print("-" * 60)
            
            print("=" * 60)
            
            # Ask for manual corrections
            user_text = input("\nEdit the text if needed (press Enter to keep): ").strip()
            
            if user_text:
                final_text = user_text
            else:
                final_text = corrected_text if original_text != corrected_text else original_text
            
            # Train model if user made corrections
            if final_text != original_text:
                save = input("\n💾 Save this correction to train the model? (y/n): ").lower()
                if save == 'y':
                    if self.ml_model.add_training_sample(original_text, final_text):
                        print("✅ Model learned this correction!")
                        self.ml_model.display_stats()
            
            # Clean up audio file
            try:
                os.remove(audio_file)
            except:
                pass
        
        except KeyboardInterrupt:
            print("\n⏹️ Recording stopped by user")
        except Exception as e:
            print(f"❌ Error: {e}")
    
    def transcribe_file(self) -> None:
        """Transcribe an existing audio file"""
        try:
            file_path = input("\nEnter path to audio file: ").strip()
            
            if not file_path:
                print("❌ No file path provided")
                return
            
            if not os.path.exists(file_path):
                print(f"❌ File not found: {file_path}")
                return
            
            print("🔄 Transcribing...")
            original_text = self.transcriber.transcribe(file_path)
            
            if not original_text:
                print("❌ Transcription failed")
                return
            
            # Apply ML corrections
            corrected_text = self.ml_model.apply_corrections(original_text)
            
            print("\n" + "=" * 60)
            print("📝 Original Transcription:")
            print("-" * 60)
            print(original_text)
            print("-" * 60)
            
            if original_text != corrected_text:
                print("\n🤖 ML Corrected Text:")
                print("-" * 60)
                print(corrected_text)
                print("-" * 60)
            
            print("=" * 60)
            
            # Ask for manual corrections
            user_text = input("\nEdit the text if needed (press Enter to keep): ").strip()
            
            if user_text:
                final_text = user_text
            else:
                final_text = corrected_text if original_text != corrected_text else original_text
            
            # Train model if user made corrections
            if final_text != original_text:
                save = input("\n💾 Save this correction to train the model? (y/n): ").lower()
                if save == 'y':
                    if self.ml_model.add_training_sample(original_text, final_text):
                        print("✅ Model learned this correction!")
                        self.ml_model.display_stats()
        
        except Exception as e:
            print(f"❌ Error: {e}")
    
    def export_data(self) -> None:
        """Export training data to file"""
        try:
            data = self.ml_model.export_data()
            filename = "voice_model_training.json"
            
            with open(filename, 'w', encoding='utf-8') as f:
                f.write(data)
            
            print(f"✅ Training data exported to {filename}")
        except Exception as e:
            print(f"❌ Export error: {e}")
    
    def import_data(self) -> None:
        """Import training data from file"""
        try:
            filename = input("Enter training data filename: ").strip()
            
            if not os.path.exists(filename):
                print(f"❌ File not found: {filename}")
                return
            
            with open(filename, 'r', encoding='utf-8') as f:
                data = f.read()
            
            if self.ml_model.import_data(data):
                print("✅ Training data imported successfully!")
                self.ml_model.display_stats()
            else:
                print("❌ Failed to import training data")
        
        except Exception as e:
            print(f"❌ Import error: {e}")
    
    def reset_model(self) -> None:
        """Reset the model"""
        confirm = input("⚠️  Are you sure? This will delete all training data. (y/n): ").lower()
        if confirm == 'y':
            self.ml_model.reset()
    
    def run(self) -> None:
        """Run the main application loop"""
        self.display_banner()
        
        while True:
            self.display_menu()
            choice = input("\nSelect an option (1-8): ").strip()
            
            if choice == "1":
                self.record_and_transcribe()
            elif choice == "2":
                self.transcribe_file()
            elif choice == "3":
                self.ml_model.display_stats()
            elif choice == "4":
                self.ml_model.show_corrections()
            elif choice == "5":
                self.export_data()
            elif choice == "6":
                self.import_data()
            elif choice == "7":
                self.reset_model()
            elif choice == "8":
                print("\n👋 Thank you for using Voice to Text Converter!")
                print("✨ Your trained model is saved and ready for next time!\n")
                break
            else:
                print("❌ Invalid option. Please try again.")


def main():
    """Entry point of the application"""
    app = VoiceToTextApp()
    
    try:
        app.run()
    except KeyboardInterrupt:
        print("\n\n👋 Application terminated by user")
        sys.exit(0)
    except Exception as e:
        print(f"\n❌ Application error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
