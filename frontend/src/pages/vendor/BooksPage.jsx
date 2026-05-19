import { useEffect, useState, useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import { fetchBooks, deleteBookThunk } from '../../store/booksSlice';
import { useDebouncedValue } from '../../hooks/useDebouncedValue';
import BookCard from '../../features/books/BookCard';
import VendorFilterSidebar from '../../features/vendor/VendorFilterSidebar';
import DeleteConfirm from '../../features/books/DeleteConfirm';
import SkeletonCard from '../../components/ui/SkeletonCard';
import EmptyState from '../../components/ui/EmptyState';
import { addToast } from '../../store/toastSlice';

const PAGE_SIZE = 12;
const DEFAULT_SORT_BY = 'createdAt';
const DEFAULT_SORT_DIR = 'desc';

export default function BooksPage() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { items, totalPages, totalElements, currentPage, pageSize, isFirst, isLast, loading, error } =
        useSelector((state) => state.books);

    const [page, setPage] = useState(0);
    const resetPage = useCallback(() => setPage(0), []);
    const [search, setSearch] = useState('');
    const debouncedSearch = useDebouncedValue(search, 350, resetPage);
    const [selectedGrades, setSelectedGrades] = useState([]);
    const [minPrice, setMinPrice] = useState('');
    const [maxPrice, setMaxPrice] = useState('');
    const [outOfStockOnly, setOutOfStockOnly] = useState(false);
    const [sortBy, setSortBy] = useState(DEFAULT_SORT_BY);
    const [sortDir, setSortDir] = useState(DEFAULT_SORT_DIR);
    const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);

    const [deleteTarget, setDeleteTarget] = useState(null);
    const [isDeleting, setIsDeleting] = useState(false);

    const hasActiveFilters =
        search.trim() !== '' ||
        selectedGrades.length > 0 ||
        minPrice !== '' ||
        maxPrice !== '' ||
        outOfStockOnly ||
        sortBy !== DEFAULT_SORT_BY ||
        sortDir !== DEFAULT_SORT_DIR;

    const handleClearAllFilters = useCallback(() => {
        setSearch('');
        setSelectedGrades([]);
        setMinPrice('');
        setMaxPrice('');
        setOutOfStockOnly(false);
        setSortBy(DEFAULT_SORT_BY);
        setSortDir(DEFAULT_SORT_DIR);
        setPage(0);
        setMobileFiltersOpen(false);
    }, []);

    const buildRequest = useCallback(
        () => ({
            page,
            size: PAGE_SIZE,
            sortBy,
            sortDir,
            search: debouncedSearch,
            minPrice,
            maxPrice,
            outOfStockOnly,
            grades: selectedGrades,
        }),
        [page, sortBy, sortDir, debouncedSearch, minPrice, maxPrice, outOfStockOnly, selectedGrades]
    );

    useEffect(() => {
        dispatch(fetchBooks(buildRequest()));
    }, [dispatch, buildRequest]);

    const handlePageChange = (next) => {
        setPage(next);
    };

    const toggleGrade = (grade) => {
        setSelectedGrades((prev) =>
            prev.includes(grade) ? prev.filter((g) => g !== grade) : [...prev, grade]
        );
        setPage(0);
    };

    const handleEdit = (book) => {
        navigate(`/dashboard/edit/${book.id}`);
    };

    const handleDelete = async () => {
        if (!deleteTarget) return;
        setIsDeleting(true);
        try {
            await dispatch(deleteBookThunk(deleteTarget.id)).unwrap();
            dispatch(addToast('Book deleted successfully', 'success'));
            setDeleteTarget(null);
            dispatch(fetchBooks(buildRequest()));
        } catch (err) {
            dispatch(addToast(err || 'Failed to delete book', 'error'));
        } finally {
            setIsDeleting(false);
        }
    };

    const filterProps = {
        search,
        onSearchChange: setSearch,
        selectedGrades,
        onToggleGrade: toggleGrade,
        minPrice,
        maxPrice,
        onMinPriceChange: (v) => {
            setMinPrice(v);
            setPage(0);
        },
        onMaxPriceChange: (v) => {
            setMaxPrice(v);
            setPage(0);
        },
        outOfStockOnly,
        onOutOfStockOnlyChange: (v) => {
            setOutOfStockOnly(v);
            setPage(0);
        },
        sortBy,
        sortDir,
        onSortByChange: (v) => {
            setSortBy(v);
            setPage(0);
        },
        onSortDirChange: (v) => {
            setSortDir(v);
            setPage(0);
        },
        hasActiveFilters,
        onClearAllFilters: handleClearAllFilters,
    };

    const skeletonCount = pageSize || PAGE_SIZE;
    const showResultCount = loading !== 'pending' || items.length > 0;
    const isEmptyInventory = totalElements === 0 && !hasActiveFilters;
    const isFilteredEmpty = totalElements === 0 && hasActiveFilters;

    if (error) {
        return (
            <div className="flex flex-col items-center justify-center py-16">
                <p className="text-sm text-red-500 dark:text-red-400 mb-4">{error}</p>
                <button
                    type="button"
                    onClick={() => dispatch(fetchBooks(buildRequest()))}
                    className="px-4 py-2 rounded-lg text-sm font-medium bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                >
                    Retry
                </button>
            </div>
        );
    }

    return (
        <div>
            <div className="mb-6 sm:mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-50">
                        My Books
                    </h1>
                    <p className="text-sm text-slate-500 dark:text-slate-400 mt-0.5">Manage your book inventory</p>
                </div>
                <div className="flex flex-wrap items-center gap-3 shrink-0">
                    {showResultCount && totalElements > 0 && (
                        <p className="text-sm text-slate-500 dark:text-slate-400 tabular-nums">
                            <span className="font-medium text-slate-700 dark:text-slate-300">{totalElements}</span>
                            {' '}
                            {totalElements === 1 ? 'book' : 'books'}
                        </p>
                    )}
                    <Link
                        to="/dashboard/add"
                        className="inline-flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                    >
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                        </svg>
                        Add Book
                    </Link>
                </div>
            </div>

            <div className="flex flex-wrap items-center justify-between gap-3 lg:hidden mb-4">
                <button
                    type="button"
                    onClick={() => setMobileFiltersOpen(true)}
                    className="inline-flex items-center gap-2 px-4 py-2.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-sm font-medium text-slate-700 dark:text-slate-200 shadow-sm"
                >
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 3c2.755 0 5.455.232 8.083.678.533.09.917.556.917 1.096v5.45a2.25 2.25 0 01-.648 1.578l-3.8 3.8a2.25 2.25 0 00-.648 1.578V19.5a2.25 2.25 0 01-2.25 2.25h-4.5A2.25 2.25 0 016 19.5v-2.153a2.25 2.25 0 00-.648-1.578l-3.8-3.8A2.25 2.25 0 012.25 9.553V4.128c0-.54.384-1.006.917-1.096A48.32 48.32 0 0112 3z" />
                    </svg>
                    Filters
                </button>
                {hasActiveFilters && (
                    <button
                        type="button"
                        onClick={handleClearAllFilters}
                        className="text-sm font-medium text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300"
                    >
                        Clear all
                    </button>
                )}
            </div>

            {mobileFiltersOpen && (
                <div
                    className="fixed inset-0 bg-black/40 z-40 lg:hidden"
                    aria-hidden
                    onClick={() => setMobileFiltersOpen(false)}
                />
            )}

            <div className="lg:grid lg:grid-cols-[minmax(288px,320px)_minmax(0,1fr)] lg:gap-8 xl:gap-10">
                <aside
                    className={`fixed inset-y-0 left-0 z-50 w-[min(100vw-2rem,22rem)] max-w-full bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 shadow-2xl p-5 sm:p-6 overflow-y-auto transition-transform duration-200 lg:static lg:z-0 lg:w-full lg:max-w-none lg:border lg:rounded-2xl lg:p-7 lg:shadow-sm lg:ring-1 lg:ring-slate-200/70 dark:lg:ring-slate-700/80 lg:border-slate-200/90 dark:lg:border-slate-700/90 lg:self-start lg:sticky lg:top-24 ${
                        mobileFiltersOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'
                    }`}
                >
                    <VendorFilterSidebar
                        {...filterProps}
                        showMobileClose
                        onCloseMobile={() => setMobileFiltersOpen(false)}
                    />
                </aside>

                <div className="min-w-0 lg:pt-0.5">
                    {loading === 'pending' && items.length === 0 ? (
                        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4 sm:gap-6">
                            {Array.from({ length: skeletonCount }).map((_, i) => (
                                <SkeletonCard key={i} />
                            ))}
                        </div>
                    ) : isEmptyInventory ? (
                        <EmptyState
                            title="No books yet"
                            description="Start building your inventory by adding your first book."
                            action={
                                <Link
                                    to="/dashboard/add"
                                    className="inline-flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-200 transition-colors"
                                >
                                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
                                        <path strokeLinecap="round" strokeLinejoin="round" d="M12 4.5v15m7.5-7.5h-15" />
                                    </svg>
                                    Add Your First Book
                                </Link>
                            }
                        />
                    ) : isFilteredEmpty ? (
                        <div className="rounded-2xl border border-dashed border-slate-200 dark:border-slate-700 bg-white/80 dark:bg-slate-900/50 px-6 py-16 text-center">
                            <p className="text-slate-600 dark:text-slate-400">No books match your filters.</p>
                            <p className="text-sm text-slate-500 dark:text-slate-500 mt-2">
                                Try adjusting search, grades, or price range.
                            </p>
                            <button
                                type="button"
                                onClick={handleClearAllFilters}
                                className="mt-6 inline-flex items-center justify-center rounded-xl border border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-800 px-4 py-2.5 text-sm font-medium text-slate-800 dark:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-700/80 transition-colors"
                            >
                                Clear all filters
                            </button>
                        </div>
                    ) : (
                        <>
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4 sm:gap-6">
                                {items.map((book) => (
                                    <BookCard
                                        key={book.id}
                                        book={book}
                                        onEdit={handleEdit}
                                        onDelete={setDeleteTarget}
                                    />
                                ))}
                            </div>

                            <div className="flex flex-col sm:flex-row items-center justify-between gap-4 mt-8 sm:mt-10">
                                <div className="text-sm text-slate-500 dark:text-slate-400">
                                    Showing{' '}
                                    <span className="font-medium text-slate-900 dark:text-slate-50">
                                        {totalElements === 0 ? 0 : currentPage * pageSize + 1}
                                    </span>{' '}
                                    to{' '}
                                    <span className="font-medium text-slate-900 dark:text-slate-50">
                                        {Math.min((currentPage + 1) * pageSize, totalElements)}
                                    </span>{' '}
                                    of{' '}
                                    <span className="font-medium text-slate-900 dark:text-slate-50">{totalElements}</span>{' '}
                                    books
                                </div>
                                {totalPages > 1 && (
                                    <div className="flex flex-wrap items-center justify-center gap-1">
                                        <button
                                            type="button"
                                            onClick={() => handlePageChange(currentPage - 1)}
                                            disabled={isFirst}
                                            className="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                                        >
                                            Previous
                                        </button>
                                        {Array.from({ length: totalPages }).map((_, i) => (
                                            <button
                                                key={i}
                                                type="button"
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
                                            type="button"
                                            onClick={() => handlePageChange(currentPage + 1)}
                                            disabled={isLast}
                                            className="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
                                        >
                                            Next
                                        </button>
                                    </div>
                                )}
                            </div>
                        </>
                    )}

                    {loading === 'pending' && items.length > 0 && (
                        <div className="fixed bottom-6 right-6 z-30 flex items-center gap-2 rounded-full bg-slate-900/90 dark:bg-slate-100/90 text-white dark:text-slate-900 px-4 py-2 text-sm font-medium shadow-lg">
                            <span className="h-4 w-4 rounded-full border-2 border-white/30 border-t-white dark:border-slate-900/30 dark:border-t-slate-900 animate-spin" />
                            Updating…
                        </div>
                    )}
                </div>
            </div>

            <DeleteConfirm
                isOpen={!!deleteTarget}
                onClose={() => setDeleteTarget(null)}
                onConfirm={handleDelete}
                book={deleteTarget}
                isDeleting={isDeleting}
            />
        </div>
    );
}
