import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Navigate } from "react-router-dom";
import { validateToken } from "../store/authSlice";

export default function PrivateRoute({ children }) {
    const dispatch = useDispatch();
    const { token, status } = useSelector((state) => state.auth);

    useEffect(() => {
        if (token && status === "idle") {
            dispatch(validateToken());
        }
    }, [token, status, dispatch]);

    if (status === "loading") {
        return <p>Loading...</p>;
    }

    if (!token || status === "unauthenticated") {
        return <Navigate to="/login" replace />;
    }

    return children;
}
