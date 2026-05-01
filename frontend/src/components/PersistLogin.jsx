import { Outlet } from "react-router-dom";
import { useState, useEffect } from "react";
import { useDispatch, useSelector } from 'react-redux';
import api from '../api/axiosClient.js';
import { setCredentials } from '../store/authSlice';

export default function PersistLogin() {
    const [isLoading, setIsLoading] = useState(true);
    const { isAuthenticated } = useSelector(state => state.auth);
    const dispatch = useDispatch();

    useEffect(() => {
        let isMounted = true;

        const verifyRefreshToken = async () => {
            try {
                // If the HttpOnly cookie is there and valid, backend will return the user data!
                const response = await api.post('/auth/refresh');

                if (isMounted) {
                    dispatch(setCredentials(response.data.data));
                }
            } catch (err) {
                console.error("No valid session found. User must log in.");
                // We don't throw an alert here, we just let them remain logged out in Redux
            } finally {
                if (isMounted) setIsLoading(false);
            }
        };

        // If Redux is empty (e.g. after a refresh), try to fetch the session
        if (!isAuthenticated) {
            verifyRefreshToken();
        } else {
            setIsLoading(false);
        }

        return () => isMounted = false; // Cleanup function
    }, [isAuthenticated, dispatch]);

    // Show a blank screen or a spinner while checking the backend
    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <p className="text-gray-600 font-medium">Verifying secure session...</p>
            </div>
        );
    }

    // Once we know who they are (or aren't), render the rest of the app!
    return <Outlet />;
}