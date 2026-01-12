import axios from 'axios';

// Smart API URL detection
// 1. Use environment variable if set
// 2. In production, construct from current window location
// 3. Fall back to localhost for development
const getApiBaseUrl = () => {
  const envUrl = import.meta.env.VITE_API_BASE_URL;

  // If explicitly set in env, use it
  if (envUrl) {
    // If it's a relative URL, resolve it against current origin
    if (envUrl.startsWith('/')) {
      return `${window.location.protocol}//${window.location.host}${envUrl}`;
    }
    return envUrl;
  }

  // Auto-detect based on environment
  if (import.meta.env.PROD) {
    // In production with combined app, use empty baseURL (relative paths)
    // This way /api/images will be relative to current origin
    return '';
  }

  // Development fallback
  return 'http://localhost:8080';
};

const API_BASE_URL = getApiBaseUrl();

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Authentication API
export const authAPI = {
  register: (data) => api.post('/api/auth/register', data),
  login: (data) => api.post('/api/auth/login', data),
  logout: () => api.post('/api/auth/logout'),
  getCurrentUser: () => api.get('/api/auth/me'),
};

// Images API
export const imagesAPI = {
  uploadImage: (formData) => {
    return api.post('/api/images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  getLatestImages: (limit = 20, offset = 0) => {
    return api.get('/api/images', {
      params: { limit, offset },
    });
  },
  getImageById: (imageId) => api.get(`/api/images/${imageId}`),
  deleteImage: (imageId) => api.delete(`/api/images/${imageId}`),
  getImageFileUrl: (imageId) => `${API_BASE_URL}/api/images/${imageId}/file`,
};

export default api;
