import { Link } from 'react-router-dom';
import { useState } from 'react';

function ClientBookCardCover({ book }) {
    const [imgError, setImgError] = useState(false);
    const imageUrl = book.pictureUrl || null;
    const inStock = book.quantity > 0;

    return (
        <div className="aspect-[4/3] bg-slate-50 dark:bg-slate-800/80 relative overflow-hidden">
                {imageUrl && !imgError ? (
                    <img
                        src={imageUrl}
                        alt={book.title}
                        className="w-full h-full object-contain object-center p-2 transition-opacity group-hover:opacity-95"
                        onError={() => setImgError(true)}
                    />
                ) : (
                    <div className="w-full h-full flex items-center justify-center p-6">
                        <svg className="w-14 h-14 text-slate-200 dark:text-slate-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.331 0 4.472.89 6.072 2.348m0-16.306A8.967 8.967 0 0118 3.75c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18c-2.331 0-4.472.89-6.072 2.348M12 6.042v16.306" />
                        </svg>
                    </div>
                )}
                <div className="absolute top-2 left-2 flex flex-wrap gap-1.5">
                    {book.grade && (
                        <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-white/95 dark:bg-slate-900/95 text-slate-700 dark:text-slate-200 border border-slate-200/80 dark:border-slate-700 shadow-sm">
                            {book.grade}
                        </span>
                    )}
                    <span
                        className={`text-xs font-medium px-2 py-0.5 rounded-full border shadow-sm ${
                            inStock
                                ? 'bg-emerald-50/95 dark:bg-emerald-950/80 text-emerald-700 dark:text-emerald-300 border-emerald-200/80 dark:border-emerald-800'
                                : 'bg-amber-50/95 dark:bg-amber-950/80 text-amber-800 dark:text-amber-200 border-amber-200/80 dark:border-amber-800'
                        }`}
                    >
                        {inStock ? 'In stock' : 'Out of stock'}
                    </span>
                </div>
            </div>
    );
}

export default function ClientBookCard({ book }) {
    const vendorUsername = book.vendor?.username;
    const coverKey = `${book.id}-${book.pictureUrl || ''}`;

    return (
        <Link
            to={`/browse/${book.id}`}
            className="group block bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 overflow-hidden shadow-sm hover:shadow-lg hover:border-slate-300 dark:hover:border-slate-600 transition-all duration-200"
        >
            <ClientBookCardCover key={coverKey} book={book} />
            <div className="p-4 sm:p-5">
                <h3 className="font-semibold text-slate-900 dark:text-slate-50 tracking-tight line-clamp-2 group-hover:text-indigo-700 dark:group-hover:text-indigo-300 transition-colors">
                    {book.title}
                </h3>
                <p className="text-sm text-slate-500 dark:text-slate-400 mt-1 line-clamp-1">{book.author}</p>
                <div className="flex items-end justify-between mt-3 gap-2">
                    <p className="text-lg font-bold text-slate-900 dark:text-slate-50">₹{book.price}</p>
                </div>
                {vendorUsername && (
                    <p className="text-xs text-slate-400 dark:text-slate-500 mt-2 truncate">
                        Sold by <span className="text-slate-500 dark:text-slate-400">@{vendorUsername}</span>
                    </p>
                )}
            </div>
        </Link>
    );
}
