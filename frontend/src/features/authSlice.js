import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import api from "../api/axios";

const savedToken = sessionStorage.getItem("token");

export const register = createAsyncThunk(
    "auth/register",
    async ({ email, password, username }, { rejectWithValue }) => {
        try {
            const response = await api.post("/auth/register", {
                email,
                username,
                password,
            });
            return response.data;
        } catch (err) {
            return rejectWithValue(err.response?.data || "Registration failed");
        }
    }
);

export const login = createAsyncThunk(
    "auth/login",
    async ({ username, password }, { rejectWithValue }) => {
        try {
            const response = await api.post("/auth/login", { username, password });

            // Server returns { accessToken, refreshToken }
            const { accessToken, refreshToken } = response.data;

            // Save tokens
            sessionStorage.setItem("token", accessToken);
            sessionStorage.setItem("refreshToken", refreshToken);

            return { token: accessToken, refreshToken };
        } catch (err) {
            return rejectWithValue(err.response?.data || "Login failed");
        }
    }
);

const authSlice = createSlice({
    name: "auth",
    initialState: { user: null, token: savedToken || null, refreshToken: null, error: null },
    reducers: {
        logout: (state) => {
            state.user = null;
            state.token = null;
            state.refreshToken = null;
            sessionStorage.removeItem("token");
            sessionStorage.removeItem("refreshToken");
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(register.fulfilled, (state, action) => {
                state.user = action.payload;
                state.error = null;
            })
            .addCase(register.rejected, (state, action) => {
                state.error = action.payload;
            })
            .addCase(login.fulfilled, (state, action) => {
                state.token = action.payload.token;
                state.refreshToken = action.payload.refreshToken;
                state.error = null;
            })
            .addCase(login.rejected, (state, action) => {
                state.error = action.payload;
            });
    },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;
