from __future__ import annotations

import json
from pathlib import Path

import joblib
import pandas as pd
from sklearn.calibration import CalibratedClassifierCV
from sklearn.compose import ColumnTransformer
from sklearn.datasets import load_breast_cancer
from sklearn.ensemble import RandomForestClassifier
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
    matthews_corrcoef,
    precision_score,
    recall_score,
    roc_auc_score,
)
from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import GaussianNB
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.svm import SVC
from sklearn.tree import DecisionTreeClassifier


PROJECT_DIR = Path(__file__).resolve().parent.parent
MODEL_DIR = PROJECT_DIR / "model"
METRICS_PATH = MODEL_DIR / "metrics.json"
SUMMARY_PATH = MODEL_DIR / "summary.json"
TEST_DATA_PATH = PROJECT_DIR / "test_data.csv"


def load_dataset() -> tuple[pd.DataFrame, pd.Series]:
    dataset = load_breast_cancer(as_frame=True)
    features = dataset.frame.drop(columns=["target"])
    target = dataset.frame["target"].rename("target")
    return features, target


def build_preprocessor(feature_names: list[str], scale_features: bool) -> ColumnTransformer:
    numeric_steps = [("imputer", SimpleImputer(strategy="median"))]
    if scale_features:
        numeric_steps.append(("scaler", StandardScaler()))

    return ColumnTransformer(
        transformers=[
            (
                "numeric",
                Pipeline(steps=numeric_steps),
                feature_names,
            )
        ]
    )


def build_models(feature_names: list[str]) -> dict[str, Pipeline]:
    return {
        "Logistic Regression": Pipeline(
            steps=[
                ("preprocessor", build_preprocessor(feature_names, scale_features=True)),
                ("model", LogisticRegression(max_iter=3000, random_state=42)),
            ]
        ),
        "Decision Tree": Pipeline(
            steps=[
                ("preprocessor", build_preprocessor(feature_names, scale_features=False)),
                (
                    "model",
                    DecisionTreeClassifier(max_depth=5, min_samples_leaf=4, random_state=42),
                ),
            ]
        ),
        "kNN": Pipeline(
            steps=[
                ("preprocessor", build_preprocessor(feature_names, scale_features=True)),
                ("model", KNeighborsClassifier(n_neighbors=9, weights="distance")),
            ]
        ),
        "Naive Bayes": Pipeline(
            steps=[
                ("preprocessor", build_preprocessor(feature_names, scale_features=False)),
                ("model", GaussianNB()),
            ]
        ),
        "Random Forest (Ensemble)": Pipeline(
            steps=[
                ("preprocessor", build_preprocessor(feature_names, scale_features=False)),
                (
                    "model",
                    RandomForestClassifier(
                        n_estimators=300,
                        max_depth=8,
                        min_samples_leaf=2,
                        random_state=42,
                    ),
                ),
            ]
        ),
        "Support Vector Machine": Pipeline(
            steps=[
                ("preprocessor", build_preprocessor(feature_names, scale_features=True)),
                (
                    "model",
                    CalibratedClassifierCV(
                        estimator=SVC(kernel="rbf", random_state=42),
                        method="sigmoid",
                        cv=5,
                        ensemble=False,
                    ),
                ),
            ]
        ),
    }


def compute_auc(model: Pipeline, features: pd.DataFrame, target: pd.Series) -> float:
    if hasattr(model, "predict_proba"):
        probabilities = model.predict_proba(features)[:, 1]
        return float(roc_auc_score(target, probabilities))

    if hasattr(model, "decision_function"):
        scores = model.decision_function(features)
        return float(roc_auc_score(target, scores))

    return 0.0


def build_observation(metrics: dict[str, float], best_accuracy: float) -> str:
    accuracy_gap = best_accuracy - metrics["accuracy"]
    if accuracy_gap < 0.005:
        return "Top-tier performer on this dataset with very strong discrimination and stable error balance."
    if metrics["recall"] > metrics["precision"]:
        return "Captures more positive-class cases, but trades precision for recall with more false positives."
    if metrics["precision"] > metrics["recall"]:
        return "Makes more conservative predictions with fewer false positives, but misses some positive cases."
    return "Delivered balanced performance across the core metrics without a clear bias toward precision or recall."


def main() -> None:
    MODEL_DIR.mkdir(parents=True, exist_ok=True)

    features, target = load_dataset()
    feature_names = features.columns.tolist()

    x_train, x_test, y_train, y_test = train_test_split(
        features,
        target,
        test_size=0.2,
        random_state=42,
        stratify=target,
    )

    test_export = x_test.copy()
    test_export["target"] = y_test.values
    test_export.to_csv(TEST_DATA_PATH, index=False)

    models = build_models(feature_names)
    metrics: dict[str, dict[str, object]] = {}

    for model_name, pipeline in models.items():
        pipeline.fit(x_train, y_train)
        predictions = pipeline.predict(x_test)
        auc_score = compute_auc(pipeline, x_test, y_test)
        matrix = confusion_matrix(y_test, predictions)
        report = classification_report(y_test, predictions, output_dict=True, zero_division=0)

        metrics[model_name] = {
            "accuracy": round(float(accuracy_score(y_test, predictions)), 4),
            "auc": round(auc_score, 4),
            "precision": round(float(precision_score(y_test, predictions, zero_division=0)), 4),
            "recall": round(float(recall_score(y_test, predictions, zero_division=0)), 4),
            "f1": round(float(f1_score(y_test, predictions, zero_division=0)), 4),
            "mcc": round(float(matthews_corrcoef(y_test, predictions)), 4),
            "confusion_matrix": matrix.tolist(),
            "classification_report": report,
        }

        artifact_name = model_name.lower().replace(" ", "_").replace("(", "").replace(")", "")
        artifact_name = artifact_name.replace("/", "_")
        joblib.dump(pipeline, MODEL_DIR / f"{artifact_name}.joblib")

    best_model = max(metrics.items(), key=lambda item: item[1]["accuracy"])
    best_accuracy = best_model[1]["accuracy"]
    for model_name, model_metrics in metrics.items():
        model_metrics["observation"] = build_observation(model_metrics, best_accuracy)

    summary = {
        "dataset_name": "Breast Cancer Wisconsin Diagnostic Dataset",
        "dataset_source": "UCI Machine Learning Repository (via scikit-learn)",
        "problem_type": "Binary classification",
        "n_instances": int(len(features)),
        "n_features": int(features.shape[1]),
        "target_distribution": {
            "malignant": int((target == 0).sum()),
            "benign": int((target == 1).sum()),
        },
        "train_size": int(len(x_train)),
        "test_size": int(len(x_test)),
        "best_model": best_model[0],
        "models": metrics,
    }

    METRICS_PATH.write_text(json.dumps(metrics, indent=2), encoding="utf-8")
    SUMMARY_PATH.write_text(json.dumps(summary, indent=2), encoding="utf-8")

    print("Saved model artifacts to", MODEL_DIR)
    print("Saved test data to", TEST_DATA_PATH)
    print("Best model:", best_model[0], "with accuracy", best_model[1]["accuracy"])


if __name__ == "__main__":
    main()