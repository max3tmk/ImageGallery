import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useSelector } from "react-redux";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ImageGallery from "./pages/ImageGallery";

function PrivateRoute({ children }) {
    const token = useSelector((state) => state.auth.token);
    return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
    return (
        <Routes>
            <Route
                path="/"
                element={
                    <PrivateRoute>
                        <ImageGallery />
                    </PrivateRoute>
                }
            />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}
