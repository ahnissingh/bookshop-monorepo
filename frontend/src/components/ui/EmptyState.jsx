export default function EmptyState({ title, description, action }) {
    return (
        <div className="flex flex-col items-center justify-center py-16 px-4">
            <div className="w-20 h-20 rounded-2xl bg-slate-100 dark:bg-slate-800 flex items-center justify-center mb-6">
                <svg className="w-10 h-10 text-slate-300 dark:text-slate-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.331 0 4.472.89 6.072 2.348m0-16.306A8.967 8.967 0 0118 3.75c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18c-2.331 0-4.472.89-6.072 2.348M12 6.042v16.306" />
                </svg>
            </div>
            <h3 className="text-lg font-semibold text-slate-900 dark:text-slate-50 mb-1">{title}</h3>
            <p className="text-sm text-slate-500 dark:text-slate-400 mb-6 text-center max-w-sm">{description}</p>
            {action && action}
        </div>
    );
}
