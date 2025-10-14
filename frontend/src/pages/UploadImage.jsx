import React, { useState } from "react";
import { useSelector } from "react-redux";
import { useDispatch } from "react-redux";
import { uploadImage } from "../features/imageSlice";

export default function UploadImage({ onUploadSuccess }) {
    const dispatch = useDispatch();
    const token = useSelector((state) => state.auth.token);
    const [file, setFile] = useState(null);
    const [description, setDescription] = useState("");
    const [error, setError] = useState(null);
    const [dragOver, setDragOver] = useState(false);

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
        setError(null);
    };

    const handleDrop = (e) => {
        e.preventDefault();
        setDragOver(false);
        setFile(e.dataTransfer.files[0]);
        setError(null);
    };

    const handleUpload = async () => {
        if (!file) {
            setError("Select file to upload.");
            return;
        }

        try {
            await dispatch(uploadImage({ file, description, token })).unwrap();
            setError(null);
            onUploadSuccess(true);
        } catch (err) {
            setError("Error loading image.");
            onUploadSuccess(false);
        }
    };

    return (
        <div>
            <h2>Upload Image</h2>

            <div
                onDragOver={(e) => {
                    e.preventDefault();
                    setDragOver(true);
                }}
                onDragLeave={() => setDragOver(false)}
                onDrop={handleDrop}
                style={{
                    border: "2px dashed #3b82f6",
                    padding: "20px",
                    textAlign: "center",
                    marginBottom: "10px",
                    backgroundColor: dragOver ? "#e0f2ff" : "#fff",
                    cursor: "pointer",
                }}
                onClick={() => document.getElementById("fileInput").click()}
            >
                {file ? file.name : "Drag and drop file here or just click to select"}
            </div>

            <input
                type="file"
                id="fileInput"
                style={{ display: "none" }}
                onChange={handleFileChange}
            />

            <input
                type="text"
                placeholder="Description (Optional)"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                style={{ display: "block", marginBottom: "10px", width: "100%", padding: "5px" }}
            />

            {error && <p style={{ color: "red" }}>{error}</p>}

            <button
                onClick={handleUpload}
                style={{
                    padding: "10px 20px",
                    backgroundColor: "#3b82f6",
                    color: "#fff",
                    border: "none",
                    cursor: "pointer",
                }}
            >
                Upload
            </button>
        </div>
    );
}