import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import api from "../api/axios";

const register = createAsyncThunk(
    "auth/register",
    async ({ username, email, password }, { rejectWithValue }) => {
        try {
            const response = await api.post("/auth/register", { username, email, password });
            return response.data;
        } catch (err) {
            return rejectWithValue(err.response?.data || "Registration failed");
        }
    }
);

const login = createAsyncThunk(
    "auth/login",
    async ({ username, password }, { rejectWithValue }) => {
        try {
            const response = await api.post("/auth/login", { username, password });
            const { accessToken, refreshToken } = response.data;

            sessionStorage.setItem("token", accessToken);
            sessionStorage.setItem("refreshToken", refreshToken);

            return { token: accessToken, refreshToken };
        } catch (err) {
            return rejectWithValue(err.response?.data || "Login failed");
        }
    }
);

const refreshAccessToken = createAsyncThunk(
    "auth/refreshToken",
    async (_, { rejectWithValue }) => {
        try {
            const refreshToken = sessionStorage.getItem("refreshToken");
            const response = await api.post("/auth/refresh", { refreshToken });
            const { accessToken } = response.data;
            sessionStorage.setItem("token", accessToken);
            return accessToken;
        } catch (err) {
            return rejectWithValue(err.response?.data || "Token refresh failed");
        }
    }
);

const authSlice = createSlice({
    name: "auth",
    initialState: {
        token: sessionStorage.getItem("token") || null,
        refreshToken: sessionStorage.getItem("refreshToken") || null,
        error: null,
        isRegistered: false,
    },
    reducers: {
        logout: (state) => {
            state.token = null;
            state.refreshToken = null;
            state.error = null;
            state.isRegistered = false;
            sessionStorage.removeItem("token");
            sessionStorage.removeItem("refreshToken");
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(register.pending, (state) => {
                state.error = null;
            })
            .addCase(register.fulfilled, (state) => {
                state.isRegistered = true;
                state.error = null;
            })
            .addCase(register.rejected, (state, action) => {
                state.error = action.payload || action.error;
            })
            .addCase(login.pending, (state) => {
                state.error = null;
            })
            .addCase(login.fulfilled, (state, action) => {
                state.token = action.payload.token;
                state.refreshToken = action.payload.refreshToken;
                state.error = null;
            })
            .addCase(login.rejected, (state, action) => {
                state.error = action.payload || action.error;
            })
            .addCase(refreshAccessToken.pending, (state) => {
            })
            .addCase(refreshAccessToken.fulfilled, (state, action) => {
                state.token = action.payload;
            })
            .addCase(refreshAccessToken.rejected, (state, action) => {
                state.error = action.payload || action.error;
            });
    },
});

export const { logout } = authSlice.actions;
export { register, login, refreshAccessToken };
export default authSlice.reducer;