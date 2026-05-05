import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { updateBookThunk } from '../../store/booksSlice';
import { getBookById } from '../../api/bookApi';
import BookForm from '../../features/books/BookForm';
import { addToast } from '../../store/toastSlice';

export default function EditBookPage() {
    const { id } = useParams();
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const [book, setBook] = useState(null);
    const [loadingBook, setLoadingBook] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        const load = async () => {
            try {
                const res = await getBookById(id);
                setBook(res.data.data);
            } catch {
                dispatch(addToast('Failed to load book', 'error'));
                navigate('/dashboard');
            } finally {
                setLoadingBook(false);
            }
        };
        load();
    }, [id, navigate, addToast]);

    const handleSubmit = async (data) => {
        setIsSubmitting(true);
        try {
            await dispatch(updateBookThunk({ id: Number(id), data })).unwrap();
            dispatch(addToast('Book updated successfully', 'success'));
            navigate('/dashboard');
        } catch (err) {
            dispatch(addToast(err || 'Failed to update book', 'error'));
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loadingBook) {
        return (
            <div className="max-w-2xl">
                <div className="animate-pulse space-y-4">
                    <div className="h-4 w-24 bg-slate-200 dark:bg-slate-800 rounded" />
                    <div className="h-6 w-48 bg-slate-200 dark:bg-slate-800 rounded" />
                    <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6 space-y-4">
                        <div className="h-10 bg-slate-100 dark:bg-slate-800 rounded-lg" />
                        <div className="h-10 bg-slate-100 dark:bg-slate-800 rounded-lg" />
                        <div className="h-10 bg-slate-100 dark:bg-slate-800 rounded-lg" />
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="max-w-2xl">
            <div className="mb-6">
                <button
                    onClick={() => navigate('/dashboard')}
                    className="flex items-center gap-1.5 text-sm text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 transition-colors mb-3"
                >
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18" />
                    </svg>
                    Back to Books
                </button>
                <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Edit Book</h1>
                <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">Update the details for {book?.title}</p>
            </div>
            <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                <BookForm
                    onSubmit={handleSubmit}
                    defaultValues={book}
                    isSubmitting={isSubmitting}
                    submitLabel="Update Book"
                />
            </div>
        </div>
    );
}
