import { useSelector } from 'react-redux';
import { Navigate, Outlet, useLocation } from 'react-router-dom';

export default function ProtectedRoute({ allowedRoles }) {
    const { isAuthenticated, roles } = useSelector((state) => state.auth);
    const location = useLocation();

    // 1 Not logged in Send to login, but remember where they were trying to go
    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    // 2Logged in, but wrong role  (e.g., Client trying to access Vendor Dashboard)
    const hasRequiredRole = roles.some((role) => allowedRoles.includes(role));
    if (!hasRequiredRole) {
        return <Navigate to="/" replace />; // Kick them to the home page
    }

    // 3 Authorized Render the child route.
    return <Outlet />;
}