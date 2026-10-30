"""
Tests for the Voice to Text ML Model
"""

import unittest
from model import VoiceToTextModel
import os


class TestVoiceToTextModel(unittest.TestCase):
    """Test cases for the ML model"""
    
    def setUp(self):
        """Set up test fixtures"""
        self.model = VoiceToTextModel("test_training.json")
        self.model.reset()
    
    def tearDown(self):
        """Clean up after tests"""
        if os.path.exists("test_training.json"):
            os.remove("test_training.json")
    
    def test_add_training_sample(self):
        """Test adding a training sample"""
        result = self.model.add_training_sample("hello world", "hello word")
        self.assertTrue(result)
        self.assertEqual(self.model.get_training_count(), 1)
    
    def test_apply_corrections(self):
        """Test applying learned corrections"""
        self.model.add_training_sample("helo", "hello")
        corrected = self.model.apply_corrections("helo world")
        self.assertEqual(corrected, "hello world")
    
    def test_apply_corrections_preserve_case(self):
        """Test that corrections preserve capitalization"""
        self.model.add_training_sample("helo", "hello")
        corrected = self.model.apply_corrections("Helo world")
        self.assertTrue(corrected.startswith("Hello"))
    
    def test_calculate_similarity(self):
        """Test similarity calculation"""
        similarity = self.model.calculate_similarity("cat", "car")
        self.assertGreater(similarity, 0.5)
        self.assertLess(similarity, 1.0)
    
    def test_levenshtein_distance(self):
        """Test Levenshtein distance calculation"""
        distance = self.model.levenshtein_distance("kitten", "sitting")
        self.assertEqual(distance, 3)
    
    def test_calculate_accuracy(self):
        """Test accuracy calculation"""
        self.model.add_training_sample("helo world", "hello world")
        accuracy = self.model.calculate_accuracy()
        self.assertGreater(accuracy, 0)
    
    def test_get_model_stats(self):
        """Test model statistics"""
        self.model.add_training_sample("test", "text")
        stats = self.model.get_model_stats()
        
        self.assertEqual(stats['status'], 'Trained')
        self.assertEqual(stats['training_samples'], 1)
        self.assertIn('accuracy', stats)
    
    def test_export_import_data(self):
        """Test exporting and importing data"""
        self.model.add_training_sample("hello", "hallo")
        exported = self.model.export_data()
        
        new_model = VoiceToTextModel("test_training2.json")
        new_model.reset()
        result = new_model.import_data(exported)
        
        self.assertTrue(result)
        self.assertEqual(new_model.get_training_count(), 1)
        
        if os.path.exists("test_training2.json"):
            os.remove("test_training2.json")
    
    def test_reset_model(self):
        """Test resetting the model"""
        self.model.add_training_sample("test", "best")
        self.assertGreater(self.model.get_training_count(), 0)
        
        self.model.reset()
        self.assertEqual(self.model.get_training_count(), 0)
    
    def test_multiple_corrections_same_word(self):
        """Test handling multiple corrections for same word"""
        self.model.add_training_sample("their", "there")
        self.model.add_training_sample("their", "there")
        self.model.add_training_sample("their", "they're")
        
        corrected = self.model.apply_corrections("their book")
        # Should use most common correction (there)
        self.assertEqual(corrected, "there book")


class TestLevenshteineDistance(unittest.TestCase):
    """Test Levenshtein distance implementation"""
    
    def setUp(self):
        """Set up test fixtures"""
        self.model = VoiceToTextModel("test_lev.json")
    
    def tearDown(self):
        """Clean up"""
        if os.path.exists("test_lev.json"):
            os.remove("test_lev.json")
    
    def test_identical_strings(self):
        """Test distance between identical strings"""
        distance = self.model.levenshtein_distance("hello", "hello")
        self.assertEqual(distance, 0)
    
    def test_one_empty_string(self):
        """Test distance with empty string"""
        distance = self.model.levenshtein_distance("hello", "")
        self.assertEqual(distance, 5)
    
    def test_single_char_difference(self):
        """Test distance with single character difference"""
        distance = self.model.levenshtein_distance("cat", "bat")
        self.assertEqual(distance, 1)


if __name__ == '__main__':
    unittest.main()
