import React, { useEffect, useState } from "react";
import { Routes, Route, Navigate, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { logout } from "./features/authSlice";
import { isTokenExpired } from "./utils/jwtUtils";

import Login from "./pages/Login";
import Register from "./pages/Register";
import MyImages from "./pages/MyImages";
import ImageGallery from "./pages/ImageGallery";
import UploadImage from "./pages/UploadImage";
import PrivateRoute from "./components/PrivateRoute";

export default function App() {
    const dispatch = useDispatch();
    const storedTokenFromStorage = sessionStorage.getItem("token");
    const isStoredTokenExpired = storedTokenFromStorage ? isTokenExpired(storedTokenFromStorage) : true;

    const effectiveToken = isStoredTokenExpired ? null : storedTokenFromStorage;

    const tokenFromRedux = useSelector((state) => state.auth?.token);
    const token = effectiveToken;
    const navigate = useNavigate();

    useEffect(() => {
        if (storedTokenFromStorage && isStoredTokenExpired) {
            console.log("Token expired — logging out");
            dispatch(logout());
            navigate("/login", { replace: true });
        }
    }, [dispatch, navigate, storedTokenFromStorage, isStoredTokenExpired]);

    useEffect(() => {
        const publicPaths = ["/login", "/register"];
        const isPublicPage = publicPaths.includes(window.location.pathname);

        if (!token && !isPublicPage) {
            navigate("/login", { replace: true });
        }
    }, [token, navigate]);

    const [activeTab, setActiveTab] = useState("my");

    const renderTabContent = () => {
        switch (activeTab) {
            case "my":
                return <MyImages />;
            case "all":
                return <ImageGallery />;
            case "upload":
                return <UploadImage onUploadSuccess={() => setActiveTab("my")} />;
            default:
                return <MyImages />;
        }
    };

    const username = token ? getUsernameFromToken(token) : "User";

    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            <Route
                path="/"
                element={
                    <PrivateRoute>
                        <div style={{ padding: "20px" }}>
                            <div style={{ display: "flex", borderBottom: "2px solid #e6eefc" }}>
                                <div
                                    onClick={() => setActiveTab("my")}
                                    style={{
                                        cursor: "pointer",
                                        padding: "10px 20px",
                                        borderBottom: activeTab === "my" ? "3px solid #2563eb" : "none",
                                        color: activeTab === "my" ? "#2563eb" : "#333",
                                        fontWeight: activeTab === "my" ? 600 : 400,
                                    }}
                                >
                                    My Images
                                </div>
                                <div
                                    onClick={() => setActiveTab("all")}
                                    style={{
                                        cursor: "pointer",
                                        padding: "10px 20px",
                                        borderBottom: activeTab === "all" ? "3px solid #2563eb" : "none",
                                        color: activeTab === "all" ? "#2563eb" : "#333",
                                        fontWeight: activeTab === "all" ? 600 : 400,
                                    }}
                                >
                                    All Images
                                </div>
                                <div
                                    onClick={() => setActiveTab("upload")}
                                    style={{
                                        cursor: "pointer",
                                        padding: "10px 20px",
                                        borderBottom: activeTab === "upload" ? "3px solid #2563eb" : "none",
                                        color: activeTab === "upload" ? "#2563eb" : "#333",
                                        fontWeight: activeTab === "upload" ? 600 : 400,
                                    }}
                                >
                                    Upload Image
                                </div>
                                <div
                                    onClick={() => {
                                        dispatch(logout());
                                        navigate("/login", { replace: true });
                                    }}
                                    style={{
                                        marginLeft: "auto",
                                        cursor: "pointer",
                                        color: "#dc2626",
                                        padding: "10px 20px",
                                        fontWeight: 500,
                                    }}
                                >
                                    <strong
                                        style={{
                                            color: "#2563eb",
                                        }}
                                    >{username}</strong> (Logout)
                                </div>
                            </div>
                            <div style={{ marginTop: "20px" }}>{renderTabContent()}</div>
                        </div>
                    </PrivateRoute>
                }
            />

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}

function getUsernameFromToken(token) {
    if (!token) return "User";
    try {
        const payloadBase64 = token.split(".")[1];
        const payload = JSON.parse(atob(payloadBase64));
        return payload.sub || payload.username || payload.email || "User";
    } catch (e) {
        console.error("Failed to decode token:", e);
        return "User";
    }
}