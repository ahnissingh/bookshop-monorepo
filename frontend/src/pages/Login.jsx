import { useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosClient.js';
import { setCredentials } from '../store/authSlice';

export default function Login() {
    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm();
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const onSubmit = async (data) => {
        try {
            const response = await api.post('/auth/login', data);

            // Extract the user data (AuthResponse)
            const userData = response.data.data;

            // 1. Update Redux State
            dispatch(setCredentials(userData));

            // 2. Route intelligently based on roles
            if (userData.roles.includes('ROLE_VENDOR')) {
                navigate('/dashboard');
            } else {
                navigate('/');
            }

        } catch (error) {
            console.error("Login failed:", error.response?.data?.message || "An error occurred");
            alert(error.response?.data?.message || "Login failed");
        }
    };

    return (
        <div className="max-w-md mx-auto mt-16 bg-white p-8 border rounded-lg shadow-sm">
            <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">Welcome Back</h2>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-gray-700">Username or Email</label>
                    <input
                        {...register("usernameOrEmail", { required: "This field is required" })}
                        className="mt-1 block w-full p-2 border rounded-md"
                        placeholder="john@example.com"
                    />
                    {errors.usernameOrEmail && <span className="text-red-500 text-sm">{errors.usernameOrEmail.message}</span>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700">Password</label>
                    <input
                        type="password"
                        {...register("password", { required: "Password is required" })}
                        className="mt-1 block w-full p-2 border rounded-md"
                        placeholder="••••••••"
                    />
                    {errors.password && <span className="text-red-500 text-sm">{errors.password.message}</span>}
                </div>

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full bg-blue-600 text-white font-bold py-2 px-4 rounded hover:bg-blue-700 disabled:opacity-50"
                >
                    {isSubmitting ? 'Logging in...' : 'Log In'}
                </button>
            </form>
            <p className="mt-4 text-center text-sm text-gray-600">
                Don't have an account? <Link to="/register" className="text-blue-600 hover:underline">Register here</Link>
            </p>
        </div>
    );
}