import { configureStore } from "@reduxjs/toolkit";
import authReducer from "../features/authSlice";
import imageReducer from "../features/imageSlice";

const store = configureStore({
    reducer: {
        auth: authReducer,
        images: imageReducer,
    },
});

export default store;