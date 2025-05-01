/**
 * Authentication Service Module
 * This service handles all authentication-related API communications:
 * 1. User registration
 * 2. User login
 * 3. User logout
 * 4. Current user information retrieval
 * 5. Authentication state management
 * 6. Google OAuth authentication
 *
 * This service uses axios interceptors to automatically add JWT tokens to authenticated requests
 * and handles unauthorized errors (401) with automatic logout.
 *
 * Tokens are stored in browser localStorage for persistent session state.
 * Follows stateless design pattern, server doesn't need to maintain session information.
 */

import axios from 'axios';
import { getAuth, signInWithPopup, GoogleAuthProvider, signOut } from "firebase/auth";
import {app} from'../Config/firebase.js';

// Base API URL - uses environment variable or defaults to localhost
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// Check if code is running in browser environment
const isBrowser = typeof window !== 'undefined' && window !== null;

/**
 * Retrieves the authentication token from localStorage
 * @returns {string|null} The authentication token or null if not found
 */
const getToken = () => {
    if (isBrowser && window.localStorage) {
        try {
            const token = window.localStorage.getItem('authToken');
            console.log("getToken called, token exists:", !!token);
            return token;
        } catch (e) {
            console.error("Error accessing localStorage:", e);
            return null;
        }
    }
    console.log("getToken called in non-browser environment");
    return null;
};

/**
 * Stores the authentication token in localStorage
 * @param {string} token - The JWT token to store
 */
const setToken = (token) => {
    console.log("Setting token:", token ? "Token provided" : "No token provided");
    if (isBrowser && window.localStorage) {
        try {
            window.localStorage.setItem('authToken', token);
            console.log("Token successfully saved to localStorage");
        } catch (e) {
            console.error("Error storing token in localStorage:", e);
        }
    } else {
        console.warn("Cannot set token: not in browser environment");
    }
};

/**
 * Removes the authentication token from localStorage
 */
const removeToken = () => {
    console.log("Removing token from localStorage");
    if (isBrowser && window.localStorage) {
        try {
            window.localStorage.removeItem('authToken');
            console.log("Token successfully removed from localStorage");
        } catch (e) {
            console.error("Error removing token from localStorage:", e);
        }
    } else {
        console.warn("Cannot remove token: not in browser environment");
    }
};

// Create an axios instance with default configuration
const apiClient = axios.create({
    baseURL: API_URL,
    headers: {
        'Content-type': 'application/json'
    }
});

/**
 * Request interceptor to add authorization token to headers
 */
apiClient.interceptors.request.use(
    (config) => {
        const token = getToken();
        console.log("Request interceptor: URL =", config.url);
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
            console.log("Added token to request headers");
        } else {
            console.log("No token available for request");
        }
        return config;
    },
    (error) => {
        console.error("Request interceptor error:", error);
        return Promise.reject(error);
    }
);

/**
 * Response interceptor to handle common errors
 * Specifically handles 401 unauthorized errors by clearing token and redirecting to login
 */
apiClient.interceptors.response.use(
    (response) => {
        console.log("Response received:", response.status, response.config.url);
        return response;
    },
    (error) => {
        console.error("Response error:", error.message);
        console.log("Response error details:", error.response ? {
            status: error.response.status,
            data: error.response.data,
            url: error.config ? error.config.url : 'unknown'
        } : 'No response details');

        // Handle 401 unauthorized errors
        if (error.response && error.response.status === 401) {
            console.warn("401 Unauthorized error detected, clearing token");
            removeToken();
            if (isBrowser) {
                console.log("Redirecting to login page");
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

// Get Firebase authentication instance (if in browser)
const auth = isBrowser ? getAuth(app) : null;

/**
 * Authentication Service API
 * Provides methods for user authentication and management
 */
const AuthService = {
    /**
     * Register a new user
     * @param {string} firstName - User's first name
     * @param {string} lastName - User's last name
     * @param {string} email - User's email address
     * @param {string} password - User's password
     * @returns {Promise<Object>} Response data from the API
     */
    signup: async (firstName, lastName, email, password) => {
        console.log("Signup attempt for:", email);
        try {
            const response = await apiClient.post('/api/auth/signup', {
                firstName,
                lastName,
                email,
                password
            });
            console.log("Signup successful, response:", response.data);

            // Store JWT token if returned by the backend
            if (response.data.token) {
                console.log("Token received from signup");
                setToken(response.data.token);
            } else {
                console.warn("No token in signup response");
            }
            return response.data;
        } catch (error) {
            console.error("Signup failed:", error.message);
            if (error.response) {
                console.error("Server response:", error.response.status, error.response.data);
            }
            throw error;
        }
    },

    /**
     * Authenticate user with email and password
     * @param {string} email - User's email address
     * @param {string} password - User's password
     * @returns {Promise<Object>} Response data from the API including token and user info
     */
    signin: async (email, password) => {
        console.log("Login attempt for:", email);
        try {
            console.log("Sending login request to:", API_URL + '/api/auth/login');
            const response = await apiClient.post('/api/auth/login', {
                email,
                password
            });

            console.log("Login response:", response.data);

            // Store token if returned by the backend
            if (response.data.token) {
                console.log("Token received from login");
                setToken(response.data.token);

                // Store user data in localStorage
                if (response.data.user) {
                    console.log("User data received:", response.data.user);
                    localStorage.setItem('currentUser', JSON.stringify(response.data.user));
                    console.log("User data saved to localStorage");
                } else {
                    console.warn("No user data in login response");
                }
            } else {
                console.warn("No token in login response");
            }

            return response.data;
        } catch (error) {
            console.error("Login failed:", error.message);
            if (error.response) {
                console.error("Server response:", error.response.status, error.response.data);
            } else {
                console.error("No server response (network error or CORS issue)");
            }
            throw error;
        }
    },

    /**
     * Authenticate user with Google OAuth
     * Uses Firebase for initial authentication, then exchanges Firebase token for backend JWT
     * @returns {Promise<Object>} Response data from the API
     */
    googleLogin: async () => {
        console.log("Google login attempt");
        if (!isBrowser) {
            console.error("Google login called in non-browser environment");
            throw new Error("Google login can only be performed in browser environment");
        }
        try {
            // Ensure Firebase Auth is initialized
            if (!auth) {
                console.error("Firebase Auth is not initialized");
                throw new Error("Firebase Auth is not initialized");
            }

            // 1. Use Firebase to authenticate with Google
            console.log("Initiating Firebase popup for Google auth");
            const provider = new GoogleAuthProvider();
            const result = await signInWithPopup(auth, provider);
            console.log("Google sign-in successful");

            // 2. Get Firebase ID token
            console.log("Getting Firebase ID token");
            const idToken = await result.user.getIdToken();
            console.log("Firebase ID token received");

            // 3. Send Firebase token to backend to get our own JWT
            console.log("Sending ID token to backend");
            const response = await apiClient.post('/api/auth/google', idToken, {
                headers: {
                    'Content-Type': 'text/plain'  // Send raw idToken
                }
            });
            console.log("Backend response to Google auth:", response.data);

            // Store our backend-generated JWT
            if (response.data.token) {
                console.log("JWT token received from backend");
                setToken(response.data.token);

                if (response.data.user) {
                    console.log("User data received:", response.data.user);
                    localStorage.setItem('currentUser', JSON.stringify(response.data.user));
                }
            } else {
                console.warn("No token in Google auth response");
            }

            return response.data;
        } catch (error) {
            console.error('Google Login error:', error);
            if (error.code) {
                console.error('Firebase error code:', error.code);
            }
            if (error.response) {
                console.error("Server response:", error.response.status, error.response.data);
            }
            throw error;
        }
    },

    /**
     * Log out the current user
     * Performs Firebase sign out (if applicable) and backend logout
     * Clears local authentication data
     * @returns {Promise<boolean>} True if logout successful
     */
    logout: async () => {
        console.log("Logout initiated");
        try {
            const token = getToken();
            console.log("Current token exists:", !!token);

            // 1. Firebase signout (if user logged in through Firebase)
            if (isBrowser && auth) {
                console.log("Attempting Firebase sign out");
                try {
                    await signOut(auth);
                    console.log("Firebase sign out successful");
                } catch (signOutError) {
                    console.warn("Firebase sign out failed:", signOutError);
                }
            }

            // 2. Notify backend about logout
            if (token) {
                console.log("Notifying backend about logout");
                try {
                    await apiClient.post('/api/auth/logout', null, {
                        headers: {
                            'Authorization': `Bearer ${token}`
                        }
                    });
                    console.log("Backend logout successful");
                } catch (err) {
                    console.warn("Backend logout failed:", err.message);
                    console.log("Continuing with client logout");
                }
            } else {
                console.log("No token to send to backend for logout");
            }

            // 3. Clear local token and user data
            console.log("Clearing local authentication data");
            removeToken();
            localStorage.removeItem('currentUser');
            console.log("Logout complete");
            return true;
        } catch (error) {
            console.error("Logout error:", error);
            console.log("Ensuring token is removed despite error");
            removeToken();
            throw error;
        }
    },

    /**
     * Get current user information
     * First checks localStorage cache, then falls back to API request if needed
     * Optimized to work with Context API
     * @returns {Promise<Object|null>} User data object or null if not authenticated
     */
    getCurrentUser: async () => {
        console.log("getCurrentUser called");

        // 首先检查是否有认证令牌
        const token = getToken();
        console.log("Token exists:", !!token);

        // 如果没有令牌，直接返回null（未认证）
        if (!token) {
            console.log("No authentication token, user is not logged in");
            return null;
        }

        // 检查本地缓存
        const cachedUser = localStorage.getItem('currentUser');
        console.log("Cached user data exists:", !!cachedUser);

        if (cachedUser) {
            try {
                const userData = JSON.parse(cachedUser);
                console.log("Using cached user data:", userData);
                return userData;
            } catch (e) {
                console.error("Error parsing cached user data:", e);
                console.log("Will try to fetch from API instead");
            }
        }

        // 如果没有缓存或解析失败，从API获取
        console.log("Fetching user data from API");
        try {
            const response = await apiClient.get('/api/auth/user');
            console.log("User data from API:", response.data);

            // 更新缓存
            if (response.data) {
                console.log("Updating user data cache");
                localStorage.setItem('currentUser', JSON.stringify(response.data));
            } else {
                console.warn("API returned empty user data");
            }

            return response.data;
        } catch (error) {
            console.error("Error fetching user data from API:", error.message);
            if (error.response) {
                console.error("API error details:", error.response.status, error.response.data);

                // 如果是401错误，清除缓存
                if (error.response.status === 401) {
                    console.warn("401 Unauthorized, clearing user cache");
                    localStorage.removeItem('currentUser');
                }
            }
            throw error;
        }
    },

    /**
     * Check if user is authenticated based on token presence
     * @returns {boolean} True if authenticated, false otherwise
     */
    isAuthenticated: () => {
        const token = getToken();
        console.log("isAuthenticated check:", !!token);
        return !!token;
    },

    /**
     * Validate a token with the backend
     * @param {string} token - The token to validate
     * @returns {Promise<boolean|Object>} Validation result
     */
    validateToken: async (token) => {
        console.log("validateToken called");
        try {
            console.log("Sending token validation request");
            const response = await apiClient.get(`/api/auth/validate?token=${token}`);
            console.log("Token validation response:", response.data);
            return response.data;
        } catch (error) {
            console.error("Token validation failed:", error.message);
            if (error.response) {
                console.error("Validation error details:", error.response.status, error.response.data);
            }
            return false;
        }
    }
};

// Export the Authentication Service
export default AuthService;