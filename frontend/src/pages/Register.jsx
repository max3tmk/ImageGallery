import React, { useState, useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { register } from "../features/authSlice";
import { useNavigate } from "react-router-dom";
import { Link } from "react-router-dom";

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

    const usernameRef = React.useRef(null);
    useEffect(() => {
        usernameRef.current?.focus();
    }, []);

    return (
        <div style={{ padding: "20px", maxWidth: "400px", margin: "50px auto" }}>
            <h2>Register</h2>
            <form onSubmit={handleSubmit}>
                <input
                    ref={usernameRef}
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    style={{ width: "100%", padding: "8px", marginBottom: "10px" }}
                />
                <input
                    type="email"
                    placeholder="Email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    style={{ width: "100%", padding: "8px", marginBottom: "10px" }}
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    style={{ width: "100%", padding: "8px", marginBottom: "10px" }}
                />
                <button
                    type="submit"
                    style={{
                        width: "100%",
                        padding: "10px",
                        backgroundColor: "#2563eb",
                        color: "white",
                        border: "none",
                        cursor: "pointer",
                    }}
                >
                    Register
                </button>
            </form>
            {error && (
                <p style={{ color: "red", marginTop: "10px" }}>
                    {typeof error === "object" ? error.message || JSON.stringify(error) : error}
                </p>
            )}
            <p style={{ marginTop: "10px", textAlign: "center" }}>
                Already have an account?{" "}
                <Link to="/login" style={{ color: "#2563eb", textDecoration: "underline" }}>
                    Login
                </Link>
            </p>
        </div>
    );
}