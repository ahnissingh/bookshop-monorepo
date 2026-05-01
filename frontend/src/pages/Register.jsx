import { useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosClient.js';
import { setCredentials } from '../store/authSlice.js';

export default function Register() {
    // Default to CLIENT role so the radio button is pre-selected
    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
        defaultValues: { role: 'CLIENT' }
    });
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const onSubmit = async (data) => {
        try {
            const { role, ...payload } = data;
            const endpoint = role === 'VENDOR' ? '/auth/register/vendor' : '/auth/register/client';

            const response = await api.post(endpoint, payload);

            dispatch(setCredentials(response.data.data));
            localStorage.setItem('isLoggedIn', 'true');
            navigate(role === 'VENDOR' ? '/dashboard' : '/'); // Vendors go to dashboard, clients to home
        } catch (error) {
            console.error("Registration failed:", error.response?.data?.message || "An error occurred");
            alert(error.response?.data?.message || "Registration failed");
        }
    };

    return (
        <div className="max-w-md mx-auto mt-16 bg-white p-8 border rounded-lg shadow-sm">
            <h2 className="text-2xl font-bold text-gray-800 mb-6 text-center">Create an Account</h2>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                {/* Role Selection (Radio Buttons) */}
                <div className="flex gap-4 p-3 bg-gray-50 border rounded-md justify-center">
                    <label className="flex items-center gap-2 cursor-pointer">
                        <input type="radio" value="CLIENT" {...register("role")} className="text-blue-600" />
                        <span className="text-sm font-medium">I am a Client</span>
                    </label>
                    <label className="flex items-center gap-2 cursor-pointer">
                        <input type="radio" value="VENDOR" {...register("role")} className="text-blue-600" />
                        <span className="text-sm font-medium">I am a Vendor</span>
                    </label>
                </div>

                <div className="flex gap-4">
                    <div className="w-1/2">
                        <label className="block text-sm font-medium text-gray-700">First Name</label>
                        <input {...register("firstName", { required: true })} className="mt-1 block w-full p-2 border rounded-md" />
                    </div>
                    <div className="w-1/2">
                        <label className="block text-sm font-medium text-gray-700">Last Name</label>
                        <input {...register("lastName", { required: true })} className="mt-1 block w-full p-2 border rounded-md" />
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700">Username</label>
                    <input {...register("username", { required: true, minLength: 3 })} className="mt-1 block w-full p-2 border rounded-md" />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700">Email</label>
                    <input type="email" {...register("email", { required: true })} className="mt-1 block w-full p-2 border rounded-md" />
                </div>

                <div>
                    <label className="block text-sm font-medium text-gray-700">Password</label>
                    <input type="password" {...register("password", { required: true, minLength: 8 })} className="mt-1 block w-full p-2 border rounded-md" />
                </div>

                <button type="submit" disabled={isSubmitting} className="w-full bg-green-600 text-white font-bold py-2 px-4 rounded hover:bg-green-700 disabled:opacity-50">
                    {isSubmitting ? 'Registering...' : 'Sign Up'}
                </button>
            </form>
            <p className="mt-4 text-center text-sm text-gray-600">
                Already have an account? <Link to="/login" className="text-blue-600 hover:underline">Log in</Link>
            </p>
        </div>
    );
}