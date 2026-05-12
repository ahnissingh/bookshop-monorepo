import { useEffect, useRef, useState } from 'react';
import { useDispatch } from 'react-redux';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import api from '../api/axiosClient.js';
import { addToast } from '../store/toastSlice';
import { getApiErrorMessage } from '../utils/apiError.js';

export default function VerifyEmail() {
    const dispatch = useDispatch();
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const navigate = useNavigate();
    const [status, setStatus] = useState('loading');
    const [errorMessage, setErrorMessage] = useState('');
    const hasFiredRef = useRef(false);

    useEffect(() => {
        if (hasFiredRef.current) return;
        hasFiredRef.current = true;

        if (!token) {
            setStatus('error');
            setErrorMessage('Verification token is missing from the URL.');
            return;
        }

        const verify = async () => {
            try {
                await api.post('/auth/verify', null, { params: { token } });
                dispatch(addToast('Email verified. You can sign in now.', 'success'));
                navigate('/login', { replace: true });
            } catch (err) {
                setStatus('error');
                setErrorMessage(
                    getApiErrorMessage(err, 'Verification failed. The link may be invalid or expired.')
                );
            }
        };

        verify();
    }, [token, navigate, dispatch]);

    return (
        <div className="min-h-[80vh] flex items-center justify-center px-4">
            <div className="w-full max-w-md text-center">
                <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-8">
                    {status === 'loading' && (
                        <>
                            <div className="mx-auto w-10 h-10 mb-4 rounded-full border-2 border-slate-200 dark:border-slate-700 border-t-slate-900 dark:border-t-slate-50 animate-spin" />
                            <h1 className="text-lg font-semibold text-slate-900 dark:text-slate-50">Verifying your email...</h1>
                            <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">Hang tight, this only takes a moment.</p>
                        </>
                    )}

                    {status === 'error' && (
                        <>
                            <div className="mx-auto w-10 h-10 mb-4 rounded-full bg-red-100 dark:bg-red-950/40 flex items-center justify-center">
                                <svg className="w-5 h-5 text-red-600 dark:text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </div>
                            <h1 className="text-lg font-semibold text-slate-900 dark:text-slate-50">Verification failed</h1>
                            <p className="text-sm text-slate-500 dark:text-slate-400 mt-2">{errorMessage}</p>
                            <Link
                                to="/"
                                className="inline-block mt-6 px-4 py-2 rounded-lg bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 text-sm font-medium hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                            >
                                Go to homepage
                            </Link>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}
