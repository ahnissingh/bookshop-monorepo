import { useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import api from '../api/axiosClient.js';
import { addToast } from '../store/toastSlice';
import { getApiErrorMessage } from '../utils/apiError.js';

export default function ForgotPassword() {
    const { register, handleSubmit, formState: { errors } } = useForm();
    const dispatch = useDispatch();
    const [submitted, setSubmitted] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    const inputClass =
        'w-full px-3 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:border-indigo-400 transition-colors';

    const onSubmit = async ({ email }) => {
        setIsSubmitting(true);
        try {
            await api.post('/auth/forgot-password', null, { params: { email: email.trim() } });
            setSubmitted(true);
        } catch (error) {
            dispatch(addToast(getApiErrorMessage(error, 'Request failed.'), 'error'));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="min-h-[80vh] flex items-center justify-center px-4">
            <div className="w-full max-w-sm">
                <div className="text-center mb-8">
                    <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Forgot password</h1>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
                        Enter your email and we will send reset instructions if an account exists.
                    </p>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                    {submitted ? (
                        <p className="text-sm text-slate-600 dark:text-slate-400 text-center">
                            If an active account exists, an email has been sent.
                        </p>
                    ) : (
                        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                            <div>
                                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
                                    Email
                                </label>
                                <input
                                    type="email"
                                    autoComplete="email"
                                    {...register('email', { required: 'Email is required' })}
                                    className={inputClass}
                                    placeholder="you@example.com"
                                />
                                {errors.email && (
                                    <p className="text-xs text-red-500 dark:text-red-400 mt-1">{errors.email.message}</p>
                                )}
                            </div>
                            <button
                                type="submit"
                                disabled={isSubmitting}
                                className="w-full py-2 rounded-lg bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 text-sm font-medium hover:bg-slate-800 dark:hover:bg-slate-200 disabled:opacity-50 transition-colors"
                            >
                                {isSubmitting ? 'Sending…' : 'Send reset link'}
                            </button>
                        </form>
                    )}
                </div>
                <p className="mt-4 text-center text-sm text-slate-500 dark:text-slate-400">
                    <Link to="/login" className="font-medium text-slate-900 dark:text-slate-50 hover:underline">
                        Back to sign in
                    </Link>
                </p>
            </div>
        </div>
    );
}
