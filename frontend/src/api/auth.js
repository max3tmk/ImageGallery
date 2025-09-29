import axios from 'axios';

const API_URL = 'http://localhost:8085'; // gateway URL

export const loginRequest = async (credentials) => {
    return axios.post(`${API_URL}/auth/login`, credentials);
};

export const registerRequest = async (data) => {
    return axios.post(`${API_URL}/auth/register`, data);
};
