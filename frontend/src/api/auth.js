const API_BASE_URL = import.meta.env.VITE_API_URL;

export const loginRequest = async (credentials) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Login failed");
    }

    return response.json();
};

export const registerRequest = async (credentials) => {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credentials),
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Registration failed");
    }

    return response.json();
};