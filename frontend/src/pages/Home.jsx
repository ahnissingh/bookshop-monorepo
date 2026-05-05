import { Link } from 'react-router-dom';
import { useSelector } from 'react-redux';

export default function Home() {
    const { isAuthenticated, roles } = useSelector((state) => state.auth);

    return (
        <div className="flex flex-col items-center justify-center min-h-[70vh] text-center">
            <div className="w-16 h-16 rounded-2xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center mb-6">
                <svg className="w-8 h-8 text-slate-400 dark:text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.331 0 4.472.89 6.072 2.348m0-16.306A8.967 8.967 0 0118 3.75c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18c-2.331 0-4.472.89-6.072 2.348M12 6.042v16.306" />
                </svg>
            </div>
            <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-50 mb-2">BookStacks</h1>
            <p className="text-slate-500 dark:text-slate-400 mb-8 max-w-md">A  marketplace for buying and selling books. Manage your inventory with ease.</p>
            {isAuthenticated && roles.includes('ROLE_VENDOR') ? (
                <Link
                    to="/dashboard"
                    className="px-5 py-2.5 rounded-lg text-sm font-medium bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                >
                    Go to Dashboard
                </Link>
            ) : !isAuthenticated ? (
                <div className="flex gap-3">
                    <Link
                        to="/login"
                        className="px-5 py-2.5 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-400 border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
                    >
                        Sign In
                    </Link>
                    <Link
                        to="/register"
                        className="px-5 py-2.5 rounded-lg text-sm font-medium bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                    >
                        Get Started
                    </Link>
                </div>
            ) : null}
        </div>
    );
}
