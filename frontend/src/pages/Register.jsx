import React, { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { register } from "../features/authSlice";
import { useNavigate } from "react-router-dom";

export default function Register() {
    const [email, setEmail] = useState("");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { error } = useSelector((state) => state.auth);

    const handleSubmit = (e) => {
        e.preventDefault();
        dispatch(register({ email, username, password })).then((res) => {
            if (!res.error) {
                navigate("/login");
            }
        });
    };

    return (
        <div>
            <h2>Register</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                />
                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                />
                <button type="submit">Register</button>
            </form>
            {error && (
                <p style={{ color: "red" }}>
                    {typeof error === "object" ? error.message || JSON.stringify(error) : error}
                </p>
            )}
        </div>
    );
}
