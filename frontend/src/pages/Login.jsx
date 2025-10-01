import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { login } from "../features/authSlice";
import { useNavigate } from "react-router-dom";

export default function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const { error, token } = useSelector((state) => state.auth);

    const handleSubmit = (e) => {
        e.preventDefault();
        dispatch(login({ username, password }));
    };

    // Redirect if login was successful (token exists)
    useEffect(() => {
        if (token) {
            navigate("/");
        }
    }, [token, navigate]);

    return (
        <div>
            <h2>Login</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <button type="submit">Login</button>
            </form>
            {error && (
                <p style={{ color: "red" }}>
                    {typeof error === "object" ? error.message || JSON.stringify(error) : error}
                </p>
            )}
        </div>
    );
}
