import { useForm } from 'react-hook-form';
import { useDispatch } from 'react-redux';
import { useNavigate, Link } from 'react-router-dom';
import { useState, useEffect } from 'react';
import api from '../api/axiosClient.js';
import { addToast } from '../store/toastSlice';
import { getApiErrorMessage, isUnverifiedAccountError } from '../utils/apiError.js';

export default function Register() {
    const { register, handleSubmit, getValues, watch, clearErrors, formState: { errors, isSubmitting } } = useForm({
        defaultValues: { role: 'CLIENT' },
    });
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [showUnverifiedHelp, setShowUnverifiedHelp] = useState(false);
    const [registerErrorDetail, setRegisterErrorDetail] = useState('');
    const [resendPending, setResendPending] = useState(false);

    const selectedRole = watch('role');
    const emailValue = watch('email');
    const passwordValue = watch('password');
    const confirmValue = watch('confirmPassword');
    const passwordsMismatchLive =
        String(passwordValue ?? '').length > 0 &&
        String(confirmValue ?? '').length > 0 &&
        passwordValue !== confirmValue;

    useEffect(() => {
        if (
            String(passwordValue ?? '').length > 0 &&
            String(confirmValue ?? '').length > 0 &&
            passwordValue === confirmValue
        ) {
            clearErrors('confirmPassword');
        }
    }, [passwordValue, confirmValue, clearErrors]);

    const onSubmit = async (data) => {
        setShowUnverifiedHelp(false);
        setRegisterErrorDetail('');
        try {
            const { role, confirmPassword, ...payload } = data;
            const endpoint = role === 'VENDOR' ? '/auth/register/vendor' : '/auth/register/client';
            await api.post(endpoint, payload);
            dispatch(addToast(
                'Account created. Please check your inbox for a verification link.',
                'success'
            ));
            navigate('/login');
        } catch (error) {
            if (isUnverifiedAccountError(error)) {
                const msg = getApiErrorMessage(
                    error,
                    'An account with this email exists but is not verified yet.'
                );
                setRegisterErrorDetail(msg);
                setShowUnverifiedHelp(true);
                dispatch(addToast(msg, 'error'));
                return;
            }
            dispatch(addToast(getApiErrorMessage(error, 'Registration failed'), 'error'));
        }
    };

    const handleResendVerification = async () => {
        const email = getValues('email');
        if (!email?.trim()) {
            dispatch(addToast('Enter the email address you used to register.', 'error'));
            return;
        }
        setResendPending(true);
        try {
            await api.post('/auth/resend-verification', null, { params: { email: email.trim() } });
            dispatch(addToast('Verification email sent. Please check your inbox.', 'success'));
        } catch (err) {
            if (err.response?.status === 429) {
                dispatch(addToast('Please wait about 2 minutes before requesting another verification email.', 'error'));
            } else {
                dispatch(addToast(getApiErrorMessage(err, 'Could not resend verification email.'), 'error'));
            }
        } finally {
            setResendPending(false);
        }
    };

    const inputClass = 'w-full px-3 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:border-indigo-400 transition-colors';
    const passwordInputClass = 'w-full pl-3 pr-10 py-2 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm placeholder-slate-400 dark:placeholder-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:border-indigo-400 transition-colors';
    const labelClass = 'block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5';

    const eyeOpen = (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z" />
            <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
        </svg>
    );
    const eyeOff = (
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.451 10.451 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.522 10.522 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" />
        </svg>
    );

    const roleCardClass = (active) =>
        `relative flex items-center justify-between gap-2 px-4 py-3 rounded-lg cursor-pointer transition-colors ${
            active
                ? 'border-2 border-slate-900 dark:border-slate-50 bg-slate-50 dark:bg-slate-800'
                : 'border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 hover:bg-slate-50 dark:hover:bg-slate-800/60'
        }`;
    const roleTitleClass = (active) =>
        `block text-sm font-semibold ${
            active ? 'text-slate-900 dark:text-slate-50' : 'text-slate-600 dark:text-slate-400'
        }`;
    const roleSubClass = (active) =>
        `block text-xs mt-0.5 ${
            active ? 'text-slate-500 dark:text-slate-400' : 'text-slate-400 dark:text-slate-500'
        }`;

    return (
        <div className="min-h-[80vh] flex items-center justify-center px-4">
            <div className="w-full max-w-md">
                <div className="text-center mb-8">
                    <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-50">Create an account</h1>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Get started with BookStacks</p>
                </div>
                <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                        <div>
                            <label className={labelClass}>I am registering as</label>
                            <div className="grid grid-cols-2 gap-3">
                                <label className={roleCardClass(selectedRole === 'CLIENT')}>
                                    <input type="radio" value="CLIENT" {...register("role")} className="sr-only" />
                                    <span>
                                        <span className={roleTitleClass(selectedRole === 'CLIENT')}>Client</span>
                                        <span className={roleSubClass(selectedRole === 'CLIENT')}>Browse and buy books</span>
                                    </span>
                                    {selectedRole === 'CLIENT' && (
                                        <svg className="w-4 h-4 text-slate-900 dark:text-slate-50 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                                        </svg>
                                    )}
                                </label>
                                <label className={roleCardClass(selectedRole === 'VENDOR')}>
                                    <input type="radio" value="VENDOR" {...register("role")} className="sr-only" />
                                    <span>
                                        <span className={roleTitleClass(selectedRole === 'VENDOR')}>Vendor</span>
                                        <span className={roleSubClass(selectedRole === 'VENDOR')}>Sell and manage books</span>
                                    </span>
                                    {selectedRole === 'VENDOR' && (
                                        <svg className="w-4 h-4 text-slate-900 dark:text-slate-50 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                                        </svg>
                                    )}
                                </label>
                            </div>
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
                            <div className="relative">
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    {...register("password", {
                                        required: "Password is required",
                                        minLength: { value: 8, message: "Password must be at least 8 characters" },
                                    })}
                                    className={passwordInputClass}
                                    placeholder="••••••••"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword((v) => !v)}
                                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                                    className="absolute inset-y-0 right-2 flex items-center px-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
                                >
                                    {showPassword ? eyeOff : eyeOpen}
                                </button>
                            </div>
                            {errors.password && <p className="text-xs text-red-500 dark:text-red-400 mt-1">{errors.password.message}</p>}
                        </div>
                        <div>
                            <label className={labelClass}>Confirm Password</label>
                            <div className="relative">
                                <input
                                    type={showConfirm ? 'text' : 'password'}
                                    {...register("confirmPassword", {
                                        required: "Please confirm your password",
                                        deps: ['password'],
                                        validate: (v) => {
                                            const pwd = getValues('password');
                                            if (!v?.length) return true;
                                            return v === pwd || 'Passwords do not match';
                                        },
                                    })}
                                    className={passwordInputClass}
                                    placeholder="••••••••"
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirm((v) => !v)}
                                    aria-label={showConfirm ? 'Hide password' : 'Show password'}
                                    className="absolute inset-y-0 right-2 flex items-center px-1.5 text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 transition-colors"
                                >
                                    {showConfirm ? eyeOff : eyeOpen}
                                </button>
                            </div>
                            {passwordsMismatchLive && (
                                <p className="text-xs text-red-500 dark:text-red-400 mt-1">Passwords do not match</p>
                            )}
                            {errors.confirmPassword && !passwordsMismatchLive && (
                                <p className="text-xs text-red-500 dark:text-red-400 mt-1">{errors.confirmPassword.message}</p>
                            )}
                        </div>
                        {showUnverifiedHelp && (
                            <div
                                className="rounded-xl border border-indigo-200/90 bg-gradient-to-br from-indigo-50 via-white to-slate-50 p-4 shadow-sm ring-1 ring-indigo-500/10 dark:border-indigo-500/35 dark:from-indigo-950/50 dark:via-slate-900 dark:to-slate-950 dark:ring-indigo-400/15 sm:p-5"
                                role="region"
                                aria-labelledby="unverified-heading"
                            >
                                <div className="flex gap-4">
                                    <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-indigo-100 text-indigo-700 shadow-inner dark:bg-indigo-900/60 dark:text-indigo-200">
                                        <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75" />
                                        </svg>
                                    </div>
                                    <div className="min-w-0 flex-1 space-y-3">
                                        <div>
                                            <h3 id="unverified-heading" className="text-sm font-semibold tracking-tight text-slate-900 dark:text-slate-100">
                                                Verify your email to continue
                                            </h3>
                                            <p className="mt-1.5 text-sm leading-relaxed text-slate-600 dark:text-slate-400">
                                                {registerErrorDetail}
                                            </p>
                                            {emailValue?.trim() ? (
                                                <p className="mt-2 text-xs text-slate-500 dark:text-slate-500">
                                                    We&apos;ll send a new link to{' '}
                                                    <span className="font-medium text-slate-700 dark:text-slate-300">{emailValue.trim()}</span>
                                                </p>
                                            ) : null}
                                        </div>
                                        <button
                                            type="button"
                                            onClick={handleResendVerification}
                                            disabled={resendPending}
                                            className="flex w-full items-center justify-center gap-2 rounded-lg bg-indigo-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-indigo-500 dark:hover:bg-indigo-400"
                                        >
                                            {resendPending ? (
                                                <>
                                                    <span className="h-4 w-4 shrink-0 animate-spin rounded-full border-2 border-white/30 border-t-white" aria-hidden />
                                                    Sending…
                                                </>
                                            ) : (
                                                <>
                                                    <svg className="h-4 w-4 shrink-0 opacity-90" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                                        <path strokeLinecap="round" strokeLinejoin="round" d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5" />
                                                    </svg>
                                                    Resend verification email
                                                </>
                                            )}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        )}
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
