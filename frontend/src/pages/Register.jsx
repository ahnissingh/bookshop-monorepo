import { useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosClient.js';
import { setCredentials } from '../store/authSlice.js';
import { addToast } from '../store/toastSlice';

export default function Register() {
    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
        defaultValues: { role: 'CLIENT' },
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
            dispatch(addToast('Account created successfully!', 'success'));
            navigate(role === 'VENDOR' ? '/dashboard' : '/');
        } catch (error) {
            dispatch(addToast(error.response?.data?.message || 'Registration failed', 'error'));
        }
    };

    const inputClass = 'w-full px-3 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:border-indigo-400 transition-colors';
    const labelClass = 'block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5';

    return (
        <div className="min-h-[80vh] flex items-center justify-center px-4">
            <div className="w-full max-w-sm">
                <div className="text-center mb-8">
                    <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Create an account</h1>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Get started with BookStacks</p>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div className="flex gap-3 p-1 bg-slate-100 dark:bg-slate-800 rounded-lg">
                            <label className="flex-1 text-center cursor-pointer">
                                <input type="radio" value="CLIENT" {...register("role")} className="sr-only peer" />
                                <span className="block py-1.5 rounded-md text-sm font-medium text-slate-500 dark:text-slate-400 peer-checked:bg-white dark:peer-checked:bg-slate-700 peer-checked:text-slate-900 dark:peer-checked:text-slate-50 peer-checked:shadow-sm transition-all">Client</span>
                            </label>
                            <label className="flex-1 text-center cursor-pointer">
                                <input type="radio" value="VENDOR" {...register("role")} className="sr-only peer" />
                                <span className="block py-1.5 rounded-md text-sm font-medium text-slate-500 dark:text-slate-400 peer-checked:bg-white dark:peer-checked:bg-slate-700 peer-checked:text-slate-900 dark:peer-checked:text-slate-50 peer-checked:shadow-sm transition-all">Vendor</span>
                            </label>
                        </div>
                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <label className={labelClass}>First Name</label>
                                <input {...register("firstName", { required: true })} className={inputClass} />
                            </div>
                            <div>
                                <label className={labelClass}>Last Name</label>
                                <input {...register("lastName", { required: true })} className={inputClass} />
                            </div>
                        </div>
                        <div>
                            <label className={labelClass}>Username</label>
                            <input {...register("username", { required: true, minLength: 3 })} className={inputClass} />
                        </div>
                        <div>
                            <label className={labelClass}>Email</label>
                            <input type="email" {...register("email", { required: true })} className={inputClass} />
                        </div>
                        <div>
                            <label className={labelClass}>Password</label>
                            <input type="password" {...register("password", { required: true, minLength: 8 })} className={inputClass} />
                        </div>
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="w-full py-2 rounded-lg bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 text-sm font-medium hover:bg-slate-800 dark:hover:bg-slate-200 disabled:opacity-50 transition-colors"
                        >
                            {isSubmitting ? 'Creating account...' : 'Create Account'}
                        </button>
                    </form>
                </div>
                <p className="mt-4 text-center text-sm text-slate-500 dark:text-slate-400">
                    Already have an account?{' '}
                    <Link to="/login" className="font-medium text-slate-900 dark:text-slate-50 hover:underline">Sign in</Link>
                </p>
            </div>
        </div>
    );
}