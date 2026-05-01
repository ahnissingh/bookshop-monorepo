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
                const response = await api.post('/auth/refresh');
                if (isMounted) {
                    dispatch(setCredentials(response.data.data));
                }
            } catch (err) {
                // If the backend rejects us (let says cookie expired),
                // we wipe the flag so we don't keep trying on future refreshes
                localStorage.removeItem('isLoggedIn');
            } finally {
                if (isMounted) setIsLoading(false);
            }
        };

        // Only hit the backend if Redux is empty AND the flag says they should be logged in
        const isLoggedInFlag = localStorage.getItem('isLoggedIn');

        if (!isAuthenticated && isLoggedInFlag === 'true') {
            verifyRefreshToken();
        } else {
            // If the flag is missing/false, skip the API call entirely!
            setIsLoading(false);
        }

        return () => isMounted = false;
    }, [isAuthenticated, dispatch]);

    if (isLoading) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <p className="text-gray-500">Loading...</p>
            </div>
        );
    }

    return <Outlet />;
}