import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getClientBookById } from '../../api/clientBookApi';
import { bookCoverSrcFromBook } from '../../utils/bookCoverSrc';

function ClientDetailHeroImage({ book }) {
    const [imgError, setImgError] = useState(false);
    const imageUrl = bookCoverSrcFromBook(book);
    const inStock = book.quantity > 0;

    return (
        <div className="rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-sm overflow-hidden">
            <div className="aspect-[3/4] bg-slate-50 dark:bg-slate-800/80 flex items-center justify-center p-4">
                {imageUrl && !imgError ? (
                    <img
                        src={imageUrl}
                        alt={book.title}
                        className="max-h-full w-full object-contain object-center"
                        onError={() => setImgError(true)}
                    />
                ) : (
                    <svg className="w-24 h-24 text-slate-200 dark:text-slate-700" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.331 0 4.472.89 6.072 2.348m0-16.306A8.967 8.967 0 0118 3.75c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18c-2.331 0-4.472.89-6.072 2.348M12 6.042v16.306" />
                    </svg>
                )}
            </div>
            <div className="px-4 pb-4 flex flex-wrap gap-2">
                {book.grade && (
                    <span className="text-xs font-semibold uppercase tracking-wide px-3 py-1 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                        {book.grade}
                    </span>
                )}
                <span
                    className={`text-xs font-semibold px-3 py-1 rounded-full ${
                        inStock
                            ? 'bg-emerald-50 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-300'
                            : 'bg-amber-50 dark:bg-amber-900/30 text-amber-800 dark:text-amber-200'
                    }`}
                >
                    {inStock ? `${book.quantity} in stock` : 'Out of stock'}
                </span>
            </div>
        </div>
    );
}

export default function ClientBookDetailPage() {
    const { id } = useParams();
    const [book, setBook] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let cancelled = false;
        const load = async () => {
            setLoading(true);
            setError(null);
            setBook(null);
            try {
                const res = await getClientBookById(id);
                if (!cancelled) {
                    setBook(res.data.data);
                }
            } catch (e) {
                if (!cancelled) {
                    setError(e.response?.data?.message || 'Could not load this book.');
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };
        if (id) load();
        return () => {
            cancelled = true;
        };
    }, [id]);

    if (loading) {
        return (
            <div className="max-w-4xl mx-auto animate-pulse space-y-6">
                <div className="h-4 w-32 rounded bg-slate-200 dark:bg-slate-800" />
                <div className="grid gap-8 md:grid-cols-2">
                    <div className="aspect-[3/4] rounded-2xl bg-slate-200 dark:bg-slate-800" />
                    <div className="space-y-4">
                        <div className="h-8 w-3/4 rounded bg-slate-200 dark:bg-slate-800" />
                        <div className="h-4 w-1/2 rounded bg-slate-200 dark:bg-slate-800" />
                        <div className="h-24 rounded-xl bg-slate-200 dark:bg-slate-800" />
                    </div>
                </div>
            </div>
        );
    }

    if (error || !book) {
        return (
            <div className="max-w-lg mx-auto text-center py-16">
                <p className="text-slate-600 dark:text-slate-400 mb-6">{error || 'Book not found.'}</p>
                <Link
                    to="/browse"
                    className="inline-flex items-center justify-center px-5 py-2.5 rounded-xl bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 text-sm font-semibold hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                >
                    Back to browse
                </Link>
            </div>
        );
    }

    const vendor = book.vendor;
    const heroKey = `${book.id}-${book.updatedAt ?? ''}`;

    return (
        <div className="max-w-4xl mx-auto">
            <Link
                to="/browse"
                className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 mb-8 transition-colors"
            >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
                </svg>
                Back to browse
            </Link>

            <div className="grid gap-8 md:grid-cols-2 md:gap-10">
                <ClientDetailHeroImage key={heroKey} book={book} />

                <div>
                    <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-50">
                        {book.title}
                    </h1>
                    {book.subtitle && (
                        <p className="mt-1 text-lg text-slate-500 dark:text-slate-400">{book.subtitle}</p>
                    )}
                    <p className="mt-2 text-base text-slate-600 dark:text-slate-300">by {book.author}</p>

                    <div className="mt-6 flex flex-wrap items-center gap-3">
                        <span className="text-3xl font-bold text-slate-900 dark:text-slate-50">₹{book.price}</span>
                    </div>

                    {vendor && (
                        <div className="mt-6 rounded-xl border border-slate-200 dark:border-slate-800 bg-slate-50/80 dark:bg-slate-800/40 px-4 py-3">
                            <p className="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">Seller</p>
                            <p className="mt-1 text-sm text-slate-800 dark:text-slate-200">
                                {[vendor.firstName, vendor.lastName].filter(Boolean).join(' ') || vendor.username}
                            </p>
                            {vendor.username && (
                                <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">@{vendor.username}</p>
                            )}
                        </div>
                    )}

                    {book.description && (
                        <div className="mt-8">
                            <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400 mb-2">
                                About this book
                            </h2>
                            <p className="text-slate-600 dark:text-slate-300 leading-relaxed whitespace-pre-wrap">
                                {book.description}
                            </p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
