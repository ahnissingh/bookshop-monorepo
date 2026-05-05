import { useSelector, useDispatch } from 'react-redux';
import { dismissToast } from '../../store/toastSlice';

const typeStyles = {
    success: 'bg-emerald-50 dark:bg-emerald-950/50 border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-200',
    error: 'bg-red-50 dark:bg-red-950/50 border-red-200 dark:border-red-800 text-red-800 dark:text-red-200',
    info: 'bg-slate-50 dark:bg-slate-800/80 border-slate-200 dark:border-slate-700 text-slate-800 dark:text-slate-200',
};

function SuccessIcon() {
    return (
        <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
        </svg>
    );
}

function ErrorIcon() {
    return (
        <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
    );
}

function InfoIcon() {
    return (
        <svg className="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
    );
}

const iconMap = { success: SuccessIcon, error: ErrorIcon, info: InfoIcon };

export default function ToastContainer() {
    const toasts = useSelector(state => state.toast.toasts);
    const dispatch = useDispatch();

    if (toasts.length === 0) return null;

    return (
        <div className="fixed top-4 right-4 z-[100] flex flex-col gap-2 max-w-sm w-full pointer-events-none">
            {toasts.map((toast) => {
                const Icon = iconMap[toast.type] || InfoIcon;
                return (
                    <div
                        key={toast.id}
                        className={`pointer-events-auto flex items-center gap-3 px-4 py-3 rounded-lg border text-sm font-medium shadow-lg backdrop-blur-sm ${typeStyles[toast.type] || typeStyles.info} ${toast.exiting ? 'animate-slide-out' : 'animate-slide-in'}`}
                    >
                        <Icon />
                        <span className="flex-1">{toast.message}</span>
                        <button
                            onClick={() => dispatch(dismissToast(toast.id))}
                            className="shrink-0 opacity-50 hover:opacity-100 transition-opacity"
                        >
                            <svg className="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                            </svg>
                        </button>
                    </div>
                );
            })}
        </div>
    );
}
