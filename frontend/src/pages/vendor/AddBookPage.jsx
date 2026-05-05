import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { createBookThunk } from '../../store/booksSlice';
import BookForm from '../../features/books/BookForm';
import { addToast } from '../../store/toastSlice';

export default function AddBookPage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (data) => {
        setIsSubmitting(true);
        try {
            await dispatch(createBookThunk(data)).unwrap();
            dispatch(addToast('Book created successfully', 'success'));
            navigate('/dashboard');
        } catch (err) {
            dispatch(addToast(err || 'Failed to create book', 'error'));
        } finally {
            setIsSubmitting(false);
        }
    };

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
                <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">Add New Book</h1>
                <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">Fill in the details to list a new book</p>
            </div>
            <div className="bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
                <BookForm onSubmit={handleSubmit} isSubmitting={isSubmitting} submitLabel="Create Book" />
            </div>
        </div>
    );
}
