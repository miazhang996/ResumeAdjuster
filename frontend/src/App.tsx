// src/App.tsx
import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Routes, Route, Navigate} from 'react-router-dom';

// @ts-ignore
import ResumeDisplay from "./assets/Components/ResumeDisplay.jsx";
// @ts-ignore
import UploadedPage1 from "./assets/Components/UploadedPage1.jsx";
// @ts-ignore
import Login from "./assets/Components/Auth/LoginPage/Login.jsx";
// @ts-ignore
import Signup from "./assets/Components/Auth/SignupPage/Signup.jsx";
// @ts-ignore
import Nav from "./assets/Components/Nav.jsx";
// @ts-ignore
import AuthService from "./assets/Services/AuthService.js";

function App() {
    // Define state for the current user and loading status
    const [currentUser, setCurrentUser] = useState<any>(null);
    const [loading, setLoading] = useState<boolean>(true); // Set to true to show initial loading state

    useEffect(() => {
        // Function to initialize authentication state
        const initializeAuth = async () => {
            try {
                // Check if user is authenticated based on token
                if (AuthService.isAuthenticated()) {
                    try {
                        // First try to get user data from localStorage cache
                        const cachedUser = localStorage.getItem('currentUser');
                        if (cachedUser) {
                            try {
                                const userData = JSON.parse(cachedUser);
                                console.log("User data from cache:", userData);
                                setCurrentUser(userData);
                                // We can still call API to validate/update user data, but UI won't be blocked
                            } catch (e) {
                                console.error("Error parsing cached user data:", e);
                            }
                        }

                        // Always fetch latest data from API regardless of cache
                        const userData = await AuthService.getCurrentUser();
                        console.log("User data fetched from API:", userData);

                        // Store in localStorage for next use
                        localStorage.setItem('currentUser', JSON.stringify(userData));

                        setCurrentUser(userData);
                    } catch (userError) {
                        console.error("Failed to fetch user data:", userError);
                        // Clear authentication data
                        AuthService.logout(); // Use service's logout instead of directly manipulating localStorage
                        setCurrentUser(null);
                    }
                } else {
                    console.log("No auth token found, user not authenticated");
                    setCurrentUser(null);
                }
            } catch (error) {
                console.error("Auth initialization error:", error);
                setCurrentUser(null);
            } finally {
                setLoading(false);
                console.log("Auth initialization completed");
            }
        };

        // Call the initialization function
        initializeAuth();
    }, []);

    // Handler for successful login - updates user state and cache
    const handleLogin = (userData: any) => {
        console.log("Login successful, setting user data:", userData);
        setCurrentUser(userData);
        // Ensure user data is saved to cache
        localStorage.setItem('currentUser', JSON.stringify(userData));
    };

    // Handler for logout - clears user state and cache
    const handleLogout = () => {
        AuthService.logout();
        setCurrentUser(null);
    };

    // Show loading indicator while authentication is being initialized
    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <Router>
            <Routes>
                {/* Public routes - accessible without authentication */}
                <Route path="/signup" element={<Signup />} />
                <Route path="/login" element={<Login onLoginSuccess={handleLogin} />} />

                {/* Routes that need navigation bar */}
                <Route path="/upload" element={
                    <>
                        <Nav currentUser={currentUser} onLogout={handleLogout} />
                        <UploadedPage1 />
                    </>
                } />
                <Route path="/display" element={
                    <>
                        <Nav currentUser={currentUser} onLogout={handleLogout} />
                        <ResumeDisplay />
                    </>
                } />

                {/* Default route - redirects to signup */}
                <Route path="/" element={<Navigate to="/signup" replace />} />
            </Routes>
        </Router>
    );
}

export default App;