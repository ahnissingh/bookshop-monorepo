import { Outlet, Link, useNavigate } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import api from "../api/axiosClient.js";
import { logout } from "../store/authSlice";

export default function Layout() {
    const { isAuthenticated, roles, username } = useSelector((state) => state.auth);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            // 1. Tell Spring Boot to destroy the HttpOnly cookies
            await api.post('/auth/logout');
        } catch (error) {
            console.error("Logout API failed, but clearing local state anyway", error);
        } finally {
            // 2. Clear the Redux "brain"
            dispatch(logout());
            // 3. Kick them back to the login page
            navigate('/login');
        }
    };

    return (
        <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow-md p-4 flex justify-between items-center">
                <div className="flex gap-4 items-center">
                    <Link to="/" className="font-bold text-blue-600">Home</Link>

                    {/* Only show Dashboard link if they are a Vendor */}
                    {isAuthenticated && roles.includes('ROLE_VENDOR') && (
                        <Link to="/dashboard" className="font-bold text-blue-600">Dashboard</Link>
                    )}
                </div>

                <div className="flex gap-4 items-center">
                    {isAuthenticated ? (
                        <>
                            <span className="text-sm text-gray-600">Hello, {username}</span>
                            <button
                                onClick={handleLogout}
                                className="bg-red-50 text-red-600 px-3 py-1 rounded hover:bg-red-100 font-medium"
                            >
                                Logout
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="text-gray-600 hover:text-blue-600 font-medium">Log In</Link>
                            <Link to="/register" className="bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700 font-medium">Sign Up</Link>
                        </>
                    )}
                </div>
            </nav>
            <main className="p-8">
                <Outlet />
            </main>
        </div>
    );
}