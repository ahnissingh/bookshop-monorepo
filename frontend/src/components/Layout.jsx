import { Outlet, Link } from "react-router-dom";

export default function Layout() {
    return (
        <div className="min-h-screen bg-gray-50">
            <nav className="bg-white shadow-md p-4 flex gap-4">
                <Link to="/" className="font-bold text-blue-600">Home</Link>
                <Link to="/dashboard" className="font-bold text-blue-600">Dashboard</Link>
            </nav>

            <main className="p-8">
                <Outlet />
            </main>
        </div>
    );
}