from __future__ import annotations

import json
from pathlib import Path

import joblib
import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
import streamlit as st
from sklearn.metrics import classification_report, confusion_matrix


PROJECT_DIR = Path(__file__).resolve().parent
MODEL_DIR = PROJECT_DIR / "model"
SUMMARY_PATH = MODEL_DIR / "summary.json"
TEST_DATA_PATH = PROJECT_DIR / "test_data.csv"


@st.cache_data
def load_summary() -> dict:
    return json.loads(SUMMARY_PATH.read_text(encoding="utf-8"))


@st.cache_resource
def load_model(model_name: str):
    artifact_name = model_name.lower().replace(" ", "_").replace("(", "").replace(")", "")
    artifact_name = artifact_name.replace("/", "_")
    return joblib.load(MODEL_DIR / f"{artifact_name}.joblib")


def render_metric_cards(model_metrics: dict) -> None:
    columns = st.columns(6)
    labels = [
        ("Accuracy", "accuracy"),
        ("AUC", "auc"),
        ("Precision", "precision"),
        ("Recall", "recall"),
        ("F1", "f1"),
        ("MCC", "mcc"),
    ]
    for column, (label, key) in zip(columns, labels):
        column.metric(label, f"{model_metrics[key]:.4f}")


def render_confusion_matrix(y_true: pd.Series, y_pred) -> None:
    matrix = confusion_matrix(y_true, y_pred)
    fig, ax = plt.subplots(figsize=(4.5, 4))
    sns.heatmap(matrix, annot=True, fmt="d", cmap="YlGnBu", cbar=False, ax=ax)
    ax.set_xlabel("Predicted")
    ax.set_ylabel("Actual")
    ax.set_title("Confusion Matrix")
    st.pyplot(fig)


def render_classification_report(y_true: pd.Series, y_pred) -> None:
    report = classification_report(y_true, y_pred, output_dict=True, zero_division=0)
    report_df = pd.DataFrame(report).transpose().round(4)
    st.dataframe(report_df, use_container_width=True)


def main() -> None:
    st.set_page_config(page_title="ML Assignment 2", page_icon="📊", layout="wide")

    st.markdown(
        """
        <style>
        .stApp {
            background: linear-gradient(135deg, #f7f4ea 0%, #eef5ff 55%, #f3fbf6 100%);
        }
        .hero {
            padding: 1.2rem 1.4rem;
            border-radius: 18px;
            background: rgba(255, 255, 255, 0.84);
            border: 1px solid rgba(28, 64, 124, 0.10);
            box-shadow: 0 12px 30px rgba(23, 44, 84, 0.08);
            margin-bottom: 1rem;
        }
        </style>
        """,
        unsafe_allow_html=True,
    )

    summary = load_summary()
    model_names = list(summary["models"].keys())

    st.markdown(
        f"""
        <div class="hero">
            <h1 style="margin-bottom:0.25rem;">Machine Learning Assignment 2</h1>
            <p style="margin:0; font-size:1.05rem;">
                Classification benchmark and deployment demo for the {summary['dataset_name']}.
            </p>
        </div>
        """,
        unsafe_allow_html=True,
    )

    with st.sidebar:
        st.header("Experiment Controls")
        selected_model = st.selectbox("Choose a model", model_names)
        uploaded_file = st.file_uploader("Upload test CSV", type=["csv"])
        st.caption("If no file is uploaded, the bundled test_data.csv is used.")

        st.header("Dataset Summary")
        st.write(f"Source: {summary['dataset_source']}")
        st.write(f"Instances: {summary['n_instances']}")
        st.write(f"Features: {summary['n_features']}")
        st.write(f"Best model: {summary['best_model']}")

    if uploaded_file is not None:
        test_df = pd.read_csv(uploaded_file)
    else:
        test_df = pd.read_csv(TEST_DATA_PATH)

    if "target" not in test_df.columns:
        st.error("The uploaded CSV must include a 'target' column so the app can compute metrics and the confusion matrix.")
        st.stop()

    x_test = test_df.drop(columns=["target"])
    y_test = test_df["target"]

    comparison_df = pd.DataFrame(summary["models"]).T[["accuracy", "auc", "precision", "recall", "f1", "mcc"]]
    comparison_df = comparison_df.sort_values(by="accuracy", ascending=False)

    left, right = st.columns([1.25, 1])

    with left:
        st.subheader("Model Comparison")
        st.dataframe(comparison_df.style.format("{:.4f}"), width="stretch")

        st.subheader(f"Selected Model: {selected_model}")
        render_metric_cards(summary["models"][selected_model])
        st.write(summary["models"][selected_model]["observation"])

    with right:
        st.subheader("Uploaded Test Data")
        st.dataframe(test_df.head(10), width="stretch")

    model = load_model(selected_model)
    predictions = model.predict(x_test)

    matrix_col, report_col = st.columns(2)
    with matrix_col:
        render_confusion_matrix(y_test, predictions)
    with report_col:
        st.subheader("Classification Report")
        render_classification_report(y_test, predictions)

    predictions_df = test_df.copy()
    predictions_df["prediction"] = predictions
    st.subheader("Prediction Preview")
    st.dataframe(predictions_df.head(15), width="stretch")


if __name__ == "__main__":
    main()