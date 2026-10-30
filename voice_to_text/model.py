"""
Machine Learning Model for Voice to Text Correction
Learns from user corrections and applies them to future transcriptions
"""

import json
import os
from typing import Dict, List, Tuple, Optional
from datetime import datetime


class VoiceToTextModel:
    """ML Model that learns from text corrections and improves over time"""
    
    def __init__(self, data_file: str = "training_data.json"):
        """
        Initialize the ML model
        
        Args:
            data_file: Path to store training data (JSON file)
        """
        self.data_file = data_file
        self.training_data: List[Dict] = []
        self.correction_dictionary: Dict[str, Dict[str, int]] = {}
        self.load_training_data()
    
    def load_training_data(self) -> None:
        """Load training data from file"""
        if os.path.exists(self.data_file):
            try:
                with open(self.data_file, 'r', encoding='utf-8') as f:
                    self.training_data = json.load(f)
                    self.build_correction_dictionary()
                    print(f"✅ Loaded {len(self.training_data)} training samples")
            except Exception as e:
                print(f"⚠️ Error loading training data: {e}")
                self.training_data = []
    
    def save_training_data(self) -> None:
        """Save training data to file"""
        try:
            with open(self.data_file, 'w', encoding='utf-8') as f:
                json.dump(self.training_data, f, indent=2, ensure_ascii=False)
        except Exception as e:
            print(f"❌ Error saving training data: {e}")
    
    def build_correction_dictionary(self) -> None:
        """Build correction dictionary from training data"""
        self.correction_dictionary = {}
        
        for pair in self.training_data:
            if pair.get('original') and pair.get('corrected'):
                original_words = pair['original'].split()
                corrected_words = pair['corrected'].split()
                
                for i in range(min(len(original_words), len(corrected_words))):
                    if original_words[i] != corrected_words[i]:
                        key = original_words[i].lower()
                        
                        if key not in self.correction_dictionary:
                            self.correction_dictionary[key] = {}
                        
                        corrected_word = corrected_words[i]
                        if corrected_word not in self.correction_dictionary[key]:
                            self.correction_dictionary[key][corrected_word] = 0
                        
                        self.correction_dictionary[key][corrected_word] += 1
    
    def add_training_sample(self, original: str, corrected: str) -> bool:
        """
        Add a training sample when user corrects text
        
        Args:
            original: Original transcribed text
            corrected: User's corrected text
        
        Returns:
            True if sample was added, False otherwise
        """
        if original.strip() != corrected.strip():
            self.training_data.append({
                'original': original.strip(),
                'corrected': corrected.strip(),
                'timestamp': datetime.now().isoformat()
            })
            self.save_training_data()
            self.build_correction_dictionary()
            return True
        return False
    
    def apply_corrections(self, text: str) -> str:
        """
        Apply learned corrections to text
        
        Args:
            text: Text to correct
        
        Returns:
            Corrected text
        """
        if not self.correction_dictionary:
            return text
        
        words = text.split()
        corrected_words = []
        
        for word in words:
            lower_word = word.lower()
            
            if lower_word in self.correction_dictionary:
                # Find the most common correction
                corrections = self.correction_dictionary[lower_word]
                best_correction = max(corrections, key=corrections.get)
                
                # Preserve original casing
                if word and word[0].isupper():
                    corrected_words.append(best_correction.capitalize())
                else:
                    corrected_words.append(best_correction)
            else:
                corrected_words.append(word)
        
        return ' '.join(corrected_words)
    
    def calculate_similarity(self, str1: str, str2: str) -> float:
        """
        Calculate similarity between two strings using Levenshtein distance
        
        Args:
            str1: First string
            str2: Second string
        
        Returns:
            Similarity score (0.0 to 1.0)
        """
        longer = str1 if len(str1) > len(str2) else str2
        shorter = str2 if len(str1) > len(str2) else str1
        
        if len(longer) == 0:
            return 1.0
        
        edit_distance = self.levenshtein_distance(longer, shorter)
        return (len(longer) - edit_distance) / len(longer)
    
    def levenshtein_distance(self, s1: str, s2: str) -> int:
        """
        Calculate Levenshtein distance between two strings
        
        Args:
            s1: First string
            s2: Second string
        
        Returns:
            Edit distance
        """
        if len(s1) < len(s2):
            return self.levenshtein_distance(s2, s1)
        
        if len(s2) == 0:
            return len(s1)
        
        previous_row = range(len(s2) + 1)
        
        for i, c1 in enumerate(s1):
            current_row = [i + 1]
            
            for j, c2 in enumerate(s2):
                insertions = previous_row[j + 1] + 1
                deletions = current_row[j] + 1
                substitutions = previous_row[j] + (c1 != c2)
                current_row.append(min(insertions, deletions, substitutions))
            
            previous_row = current_row
        
        return previous_row[-1]
    
    def calculate_accuracy(self) -> int:
        """
        Calculate model accuracy based on training data
        
        Returns:
            Accuracy percentage (0-100)
        """
        if not self.training_data:
            return 0
        
        correct_count = 0
        
        for pair in self.training_data:
            original = pair['original']
            corrected = pair['corrected']
            predicted = self.apply_corrections(original)
            
            similarity = self.calculate_similarity(predicted, corrected)
            if similarity > 0.7:
                correct_count += 1
        
        return round((correct_count / len(self.training_data)) * 100)
    
    def get_model_stats(self) -> Dict:
        """
        Get current model statistics
        
        Returns:
            Dictionary with model statistics
        """
        status = "Trained" if self.training_data else "Untrained"
        accuracy = self.calculate_accuracy()
        
        return {
            'status': status,
            'training_samples': len(self.training_data),
            'accuracy': accuracy,
            'dictionary_size': len(self.correction_dictionary)
        }
    
    def reset(self) -> None:
        """Reset the model and clear all training data"""
        self.training_data = []
        self.correction_dictionary = {}
        
        if os.path.exists(self.data_file):
            os.remove(self.data_file)
        
        print("🔄 Model reset successfully")
    
    def export_data(self) -> str:
        """
        Export training data as JSON string
        
        Returns:
            JSON string of training data
        """
        return json.dumps(self.training_data, indent=2, ensure_ascii=False)
    
    def import_data(self, json_string: str) -> bool:
        """
        Import training data from JSON string
        
        Args:
            json_string: JSON string with training data
        
        Returns:
            True if import successful, False otherwise
        """
        try:
            data = json.loads(json_string)
            if isinstance(data, list):
                self.training_data = data
                self.save_training_data()
                self.build_correction_dictionary()
                return True
        except Exception as e:
            print(f"❌ Import error: {e}")
        
        return False
    
    def display_stats(self) -> None:
        """Display model statistics in a formatted way"""
        stats = self.get_model_stats()
        
        print("\n" + "=" * 50)
        print("🤖 Model Statistics")
        print("=" * 50)
        print(f"Status:              {stats['status']}")
        print(f"Training Samples:    {stats['training_samples']}")
        print(f"Model Accuracy:      {stats['accuracy']}%")
        print(f"Correction Patterns: {stats['dictionary_size']}")
        print("=" * 50 + "\n")
    
    def get_training_count(self) -> int:
        """Get number of training samples"""
        return len(self.training_data)
    
    def show_corrections(self) -> None:
        """Display learned corrections"""
        if not self.correction_dictionary:
            print("No corrections learned yet.")
            return
        
        print("\n" + "=" * 50)
        print("📚 Learned Corrections")
        print("=" * 50)
        
        for original, corrections in sorted(self.correction_dictionary.items()):
            best = max(corrections, key=corrections.get)
            count = corrections[best]
            print(f"'{original}' → '{best}' ({count} times)")
        
        print("=" * 50 + "\n")
