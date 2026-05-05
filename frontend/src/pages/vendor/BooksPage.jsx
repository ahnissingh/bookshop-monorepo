import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { fetchBooks, deleteBookThunk, uploadImageThunk } from '../../store/booksSlice';
import BookCard from '../../features/books/BookCard';
import DeleteConfirm from '../../features/books/DeleteConfirm';
import ImageUpload from '../../features/books/ImageUpload';
import SkeletonCard from '../../components/ui/SkeletonCard';
import EmptyState from '../../components/ui/EmptyState';
import { addToast } from '../../store/toastSlice';

export default function BooksPage() {
    const dispatch = useDispatch();
    const { items, totalPages, currentPage, loading, error } = useSelector((state) => state.books);
    const navigate = useNavigate();

    const [deleteTarget, setDeleteTarget] = useState(null);
    const [uploadTarget, setUploadTarget] = useState(null);
    const [isDeleting, setIsDeleting] = useState(false);

    useEffect(() => {
        dispatch(fetchBooks({ page: 0, size: 12 }));
    }, [dispatch]);

    const handlePageChange = (page) => {
        dispatch(fetchBooks({ page, size: 12 }));
    };

    const handleDelete = async () => {
        if (!deleteTarget) return;
        setIsDeleting(true);
        try {
            await dispatch(deleteBookThunk(deleteTarget.id)).unwrap();
            dispatch(addToast('Book deleted successfully', 'success'));
            setDeleteTarget(null);
        } catch (err) {
            dispatch(addToast(err || 'Failed to delete book', 'error'));
        } finally {
            setIsDeleting(false);
        }
    };

    const handleUpload = async (id, file) => {
        try {
            await dispatch(uploadImageThunk({ id, file })).unwrap();
            dispatch(addToast('Image uploaded successfully', 'success'));
            dispatch(fetchBooks({ page: currentPage, size: 12 }));
        } catch (err) {
            dispatch(addToast(err || 'Failed to upload image', 'error'));
        }
    };

    const handleEdit = (book) => {
        navigate(`/dashboard/edit/${book.id}`);
    };

    if (error) {
        return (
            <div className="flex flex-col items-center justify-center py-16">
                <p className="text-sm text-red-500 dark:text-red-400 mb-4">{error}</p>
                <button
                    onClick={() => dispatch(fetchBooks({ page: 0, size: 12 }))}
                    className="px-4 py-2 rounded-lg text-sm font-medium bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                >
                    Retry
                </button>
            </div>
        );
    }

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <div>
                    <h1 className="text-xl font-bold text-slate-900 dark:text-slate-50">My Books</h1>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">Manage your book inventory</p>
                </div>
                <Link
                    to="/dashboard/add"
                    className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                >
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                    </svg>
                    Add Book
                </Link>
            </div>

            {loading === 'pending' && items.length === 0 ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {Array.from({ length: 8 }).map((_, i) => (
                        <SkeletonCard key={i} />
                    ))}
                </div>
            ) : items.length === 0 ? (
                <EmptyState
                    title="No books yet"
                    description="Start building your inventory by adding your first book."
                    action={
                        <Link
                            to="/dashboard/add"
                            className="flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                        >
                            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                            </svg>
                            Add Your First Book
                        </Link>
                    }
                />
            ) : (
                <>
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                        {items.map((book) => (
                            <BookCard
                                key={book.id}
                                book={book}
                                onEdit={handleEdit}
                                onDelete={setDeleteTarget}
                                onUploadImage={setUploadTarget}
                            />
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <div className="flex items-center justify-center gap-1 mt-8">
                            <button
                                onClick={() => handlePageChange(currentPage - 1)}
                                disabled={currentPage === 0}
                                className="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                            >
                                Previous
                            </button>
                            {Array.from({ length: totalPages }).map((_, i) => (
                                <button
                                    key={i}
                                    onClick={() => handlePageChange(i)}
                                    className={`w-8 h-8 rounded-lg text-sm font-medium transition-colors ${
                                        i === currentPage
                                            ? 'bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900'
                                            : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800'
                                    }`}
                                >
                                    {i + 1}
                                </button>
                            ))}
                            <button
                                onClick={() => handlePageChange(currentPage + 1)}
                                disabled={currentPage >= totalPages - 1}
                                className="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                            >
                                Next
                            </button>
                        </div>
                    )}
                </>
            )}

            <DeleteConfirm
                isOpen={!!deleteTarget}
                onClose={() => setDeleteTarget(null)}
                onConfirm={handleDelete}
                book={deleteTarget}
                isDeleting={isDeleting}
            />

            <ImageUpload
                isOpen={!!uploadTarget}
                onClose={() => setUploadTarget(null)}
                onUpload={handleUpload}
                book={uploadTarget}
            />
        </div>
    );
}
