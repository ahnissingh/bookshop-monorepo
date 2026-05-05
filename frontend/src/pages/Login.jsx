import { useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosClient.js';
import { setCredentials } from '../store/authSlice';
import { addToast } from '../store/toastSlice';

export default function Login() {
    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const onSubmit = async (data) => {
        try {
            const response = await api.post('/auth/login', data);
            const userData = response.data.data;
            dispatch(setCredentials(userData));
            localStorage.setItem('isLoggedIn', 'true');
            dispatch(addToast(`Welcome back, ${userData.username}!`, 'success'));
            if (userData.roles.includes('ROLE_VENDOR')) {
                navigate('/dashboard');
            } else {
                navigate('/');
            }
        } catch (error) {
            dispatch(addToast(error.response?.data?.message || 'Login failed', 'error'));
        }
    };

    return (
        <div className="min-h-[80vh] flex items-center justify-center px-4">
            <div className="w-full max-w-sm">
                <div className="text-center mb-8">
                    <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Welcome back</h1>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Sign in to your account</p>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">Username or Email</label>
                            <input
                                {...register("usernameOrEmail", { required: "This field is required" })}
                                className="w-full px-3 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:border-indigo-400 transition-colors"
                                placeholder="john@example.com"
                            />
                            {errors.usernameOrEmail && <p className="text-xs text-red-500 dark:text-red-400 mt-1">{errors.usernameOrEmail.message}</p>}
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">Password</label>
                            <input
                                type="password"
                                {...register("password", { required: "Password is required" })}
                                className="w-full px-3 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:border-indigo-400 transition-colors"
                                placeholder="••••••••"
                            />
                            {errors.password && <p className="text-xs text-red-500 dark:text-red-400 mt-1">{errors.password.message}</p>}
                        </div>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="w-full py-2 rounded-lg bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 text-sm font-medium hover:bg-slate-800 dark:hover:bg-slate-200 disabled:opacity-50 transition-colors"
                        >
                            {isSubmitting ? 'Signing in...' : 'Sign In'}
                        </button>
                    </form>
                </div>
                <p className="mt-4 text-center text-sm text-slate-500 dark:text-slate-400">
                    Don't have an account?{' '}
                    <Link to="/register" className="font-medium text-slate-900 dark:text-slate-50 hover:underline">Register</Link>
                </p>
            </div>
        </div>
    );
}