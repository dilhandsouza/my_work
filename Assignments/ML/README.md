# Machine Learning Assignment 2

## Problem statement
Build, evaluate, and deploy multiple classification models on one public dataset, then expose the results through an interactive Streamlit application.

## Dataset description
This solution uses the Breast Cancer Wisconsin Diagnostic dataset from the UCI Machine Learning Repository, accessed through scikit-learn. It is a binary classification problem with 569 instances and 30 numerical features, which satisfies the assignment constraints of at least 500 instances and at least 12 features. The target classes represent malignant and benign tumors.

## Github Repository Link
Repository: https://github.com/dilhandsouza/my_work

Project folder: Assignments/ML

## Models used
The following models are implemented in this project on the same dataset:

1. Logistic Regression
2. Decision Tree
3. k-Nearest Neighbors
4. Gaussian Naive Bayes
5. Random Forest
6. Support Vector Machine

The PDF text explicitly names five required models but also says that all six models must be implemented. To satisfy that wording conservatively, this project includes the five named models plus a Support Vector Machine baseline.

| ML Model Name | Accuracy | AUC | Precision | Recall | F1 | MCC |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Logistic Regression | 0.9825 | 0.9954 | 0.9861 | 0.9861 | 0.9861 | 0.9623 |
| Decision Tree | 0.9035 | 0.9358 | 0.9420 | 0.9028 | 0.9220 | 0.7969 |
| kNN | 0.9737 | 0.9934 | 0.9600 | 1.0000 | 0.9796 | 0.9442 |
| Naive Bayes | 0.9386 | 0.9878 | 0.9452 | 0.9583 | 0.9517 | 0.8676 |
| Random Forest (Ensemble) | 0.9561 | 0.9944 | 0.9589 | 0.9722 | 0.9655 | 0.9054 |
| Support Vector Machine | 0.9825 | 0.9950 | 0.9861 | 0.9861 | 0.9861 | 0.9623 |

### Model observations

| ML Model Name | Observation about model performance |
| --- | --- |
| Logistic Regression | Best overall performer in this run, with the highest AUC and a strong balance of precision and recall. |
| Decision Tree | Fast and interpretable, but it produced the weakest overall accuracy and MCC among the compared models. |
| kNN | Very strong recall and F1, showing that distance-based classification works well after feature scaling on this dataset. |
| Naive Bayes | Good probabilistic baseline with competitive recall, though its independence assumption reduces overall accuracy. |
| Random Forest (Ensemble) | More stable than the single decision tree and much stronger on AUC, but still short of the top linear and margin-based models. |
| Support Vector Machine | Matched Logistic Regression on accuracy and MCC, giving another top-tier result with strong separation between classes. |
| Overall Winner for your dataset? | Logistic Regression is the overall winner because it achieved the top accuracy, the best AUC, and the best MCC while remaining simple and deployable. |

### Streamlit app features

1. CSV test-data upload
2. Model selection dropdown
3. Metric display for the selected model
4. Confusion matrix and classification report
5. Prediction preview on uploaded test rows

## Project structure
```text
Assignments/ML/
|-- app.py
|-- README.md
|-- requirements.txt
|-- test_data.csv
|-- model/
|   |-- train_models.py
|   |-- *.joblib
|   |-- metrics.json
|   |-- summary.json
|-- ML_Assignment_2.pdf
```

## Run locally
```bash
python model/train_models.py
streamlit run app.py
```

## Submission notes
1. `test_data.csv` is generated from the held-out test split and is ready for Streamlit upload.
2. The Streamlit app shows dataset upload, model selection, evaluation metrics, confusion matrix, classification report, and predictions.
3. Add the required BITS Virtual Lab screenshot to the final submitted PDF manually.
4. Deploying to Streamlit Community Cloud still requires your GitHub-connected sign-in from the browser, but the repository contents needed for deployment are now in place.