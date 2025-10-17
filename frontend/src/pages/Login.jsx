import React, { useState, useEffect, useCallback } from "react";
import { useDispatch, useSelector } from "react-redux";
import { login } from "../features/authSlice";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";

export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [submitted, setSubmitted] = useState(false);

    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { token, error } = useSelector((state) => state.auth);

    const handleSubmit = useCallback(() => {
        setSubmitted(true);
        dispatch(login({ username, password }));
    }, [username, password, dispatch]);

    useEffect(() => {
        if (token && submitted) {
            navigate("/", { replace: true });
        }
    }, [token, submitted, navigate]);

    const handleKeyDown = (e) => {
        if (e.key === "Enter") handleSubmit();
    };

    const usernameRef = React.useRef(null);
    useEffect(() => {
        usernameRef.current?.focus();
    }, []);

    return (
        <div style={{ padding: "20px", maxWidth: "400px", margin: "50px auto" }}>
            <h2>Login</h2>
            <div>
                <input
                    ref={usernameRef}
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    onKeyDown={handleKeyDown}
                    style={{ width: "100%", padding: "8px", marginBottom: "10px" }}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onKeyDown={handleKeyDown}
                    style={{ width: "100%", padding: "8px", marginBottom: "10px" }}
                />
                <button
                    type="button"
                    onClick={handleSubmit}
                    style={{
                        width: "100%",
                        padding: "10px",
                        backgroundColor: "#2563eb",
                        color: "white",
                        border: "none",
                        cursor: "pointer",
                    }}
                >
                    Login
                </button>
            </div>
            <p style={{ marginTop: "10px", textAlign: "center" }}>
                Don't have an account?{" "}
                <Link to="/register" style={{ color: "#2563eb", textDecoration: "underline" }}>
                    Register
                </Link>
            </p>
            {error && submitted && (
                <p style={{ color: "red", marginTop: "10px" }}>
                    {typeof error === "object" ? error.message || JSON.stringify(error) : error}
                </p>
            )}
        </div>
    );
}