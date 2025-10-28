import api from "./axios";

export const getAllImages = async (page = 0, size = 10) => {
    const response = await api.get(`/images?page=${page}&size=${size}`);
    return response.data;
};

export const getUserImages = async (userId, page = 0, size = 10) => {
    const response = await api.get(`/user/${userId}/images?page=${page}&size=${size}`);
    return response.data;
};