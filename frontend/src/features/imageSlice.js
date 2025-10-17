import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import api from "../api/axios";

// === Thunks ===
export const fetchAllImages = createAsyncThunk(
    "images/fetchAll",
    async ({ page, size }, { rejectWithValue }) => {
        try {
            const response = await api.get(`/images?page=${page}&size=${size}`);
            return response.data;
        } catch (err) {
            return rejectWithValue(err.response?.data || "Failed to fetch all images");
        }
    }
);

export const fetchUserImages = createAsyncThunk(
    "images/fetchUser",
    async ({ userId, page, size }, { rejectWithValue }) => {
        try {
            const response = await api.get(`/user/${userId}/images?page=${page}&size=${size}`);
            return response.data;
        } catch (err) {
            return rejectWithValue(err.response?.data || "Failed to fetch user images");
        }
    }
);

export const uploadImage = createAsyncThunk(
    "images/uploadImage",
    async ({ file, description, token }, { rejectWithValue }) => {
        try {
            const formData = new FormData();
            formData.append("file", file);
            formData.append("description", description || "");

            const response = await api.post("/images", formData, {
                headers: {
                    "Content-Type": "multipart/form-data",
                    Authorization: `Bearer ${token}`,
                },
            });

            return response.data;
        } catch (err) {
            return rejectWithValue(err.response?.data || "Failed to upload image");
        }
    }
);

// === Slice ===
const imagesSlice = createSlice({
    name: "images",
    initialState: {
        all: { content: [], totalPages: 0 },
        user: { content: [], totalPages: 0 },
        status: "idle",
        error: null,
    },
    reducers: {},
    extraReducers: (builder) => {
        builder
            // Fetch all
            .addCase(fetchAllImages.pending, (state) => {
                state.status = "loading";
            })
            .addCase(fetchAllImages.fulfilled, (state, action) => {
                state.status = "succeeded";
                state.all = action.payload;
            })
            .addCase(fetchAllImages.rejected, (state, action) => {
                state.status = "failed";
                state.error = action.payload;
            })

            // Fetch user
            .addCase(fetchUserImages.pending, (state) => {
                state.status = "loading";
            })
            .addCase(fetchUserImages.fulfilled, (state, action) => {
                state.status = "succeeded";
                state.user = action.payload;
            })
            .addCase(fetchUserImages.rejected, (state, action) => {
                state.status = "failed";
                state.error = action.payload;
            })

            // Upload image
      .addCase(uploadImage.pending, (state) => {
        state.status = "loading";
      })
      .addCase(uploadImage.fulfilled, (state, action) => {
        state.status = "succeeded";
        state.user.content.unshift(action.payload);
      })
      .addCase(uploadImage.rejected, (state, action) => {
        state.status = "failed";
        state.error = action.payload;
      });
  },
});

export default imagesSlice.reducer;