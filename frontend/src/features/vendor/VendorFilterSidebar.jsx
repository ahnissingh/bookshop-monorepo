import { CLIENT_GRADE_OPTIONS } from '../client/clientGradeConstants';

export default function VendorFilterSidebar({
    search,
    onSearchChange,
    selectedGrades,
    onToggleGrade,
    minPrice,
    maxPrice,
    onMinPriceChange,
    onMaxPriceChange,
    outOfStockOnly,
    onOutOfStockOnlyChange,
    sortBy,
    sortDir,
    onSortByChange,
    onSortDirChange,
    onCloseMobile,
    showMobileClose,
    hasActiveFilters,
    onClearAllFilters,
}) {
    const label = 'block text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400 mb-2';
    const input =
        'w-full px-3 py-2.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-50 text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 dark:focus:border-indigo-400';

    const clearBtnClass =
        'text-sm font-medium rounded-lg px-2 py-1 -mr-1 transition-colors disabled:opacity-35 disabled:cursor-not-allowed disabled:hover:bg-transparent ' +
        'text-indigo-600 dark:text-indigo-400 hover:text-indigo-800 dark:hover:text-indigo-300 hover:bg-indigo-50 dark:hover:bg-indigo-950/40 ' +
        'focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500/40 focus-visible:ring-offset-2 dark:focus-visible:ring-offset-slate-900';

    return (
        <div className="space-y-7">
            {showMobileClose && (
                <div className="flex items-center justify-between lg:hidden pb-3 border-b border-slate-200 dark:border-slate-800">
                    <h2 className="text-base font-semibold tracking-tight text-slate-900 dark:text-slate-50">Filters</h2>
                    <div className="flex items-center gap-1">
                        <button type="button" onClick={onClearAllFilters} disabled={!hasActiveFilters} className={clearBtnClass}>
                            Clear all
                        </button>
                        <button
                            type="button"
                            onClick={onCloseMobile}
                            className="text-sm font-semibold text-indigo-600 dark:text-indigo-400 hover:underline px-2 py-1 rounded-lg"
                        >
                            Done
                        </button>
                    </div>
                </div>
            )}

            <div className="hidden lg:flex items-center justify-between gap-3 pb-4 border-b border-slate-200 dark:border-slate-800">
                <div>
                    <p className="text-xs font-semibold uppercase tracking-wide text-slate-500 dark:text-slate-400">Refine by</p>
                    <h2 className="text-base font-semibold tracking-tight text-slate-900 dark:text-slate-50 mt-0.5">Filters</h2>
                </div>
                <button type="button" onClick={onClearAllFilters} disabled={!hasActiveFilters} className={clearBtnClass}>
                    Clear all
                </button>
            </div>

            <div>
                <label className={label} htmlFor="vendor-filter-search">Search</label>
                <input
                    id="vendor-filter-search"
                    type="search"
                    value={search}
                    onChange={(e) => onSearchChange(e.target.value)}
                    placeholder="Title, author, or subtitle"
                    className={input}
                />
            </div>

            <div>
                <span className={label}>Grade</span>
                <div
                    className="client-filter-grade-scroll space-y-2.5 max-h-56 overflow-y-auto rounded-xl border border-slate-200 bg-slate-50/90 p-3 pr-2 dark:border-slate-700 dark:bg-slate-800/70"
                >
                    {CLIENT_GRADE_OPTIONS.map((grade) => (
                        <label key={grade} className="flex items-start gap-2.5 cursor-pointer group">
                            <input
                                type="checkbox"
                                checked={selectedGrades.includes(grade)}
                                onChange={() => onToggleGrade(grade)}
                                className="mt-0.5 rounded border-slate-300 dark:border-slate-600 text-indigo-600 focus:ring-indigo-500/30 shrink-0"
                            />
                            <span className="text-sm leading-snug text-slate-700 dark:text-slate-300 group-hover:text-slate-900 dark:group-hover:text-slate-100">
                                {grade}
                            </span>
                        </label>
                    ))}
                </div>
            </div>

            <div>
                <span className={label}>Price (₹)</span>
                <div className="grid grid-cols-2 gap-2.5">
                    <div>
                        <label className="sr-only" htmlFor="min-price">Minimum</label>
                        <input
                            id="vendor-min-price"
                            type="number"
                            min="0"
                            step="0.01"
                            placeholder="Min"
                            value={minPrice}
                            onChange={(e) => onMinPriceChange(e.target.value)}
                            className={input}
                        />
                    </div>
                    <div>
                        <label className="sr-only" htmlFor="max-price">Maximum</label>
                        <input
                            id="vendor-max-price"
                            type="number"
                            min="0"
                            step="0.01"
                            placeholder="Max"
                            value={maxPrice}
                            onChange={(e) => onMaxPriceChange(e.target.value)}
                            className={input}
                        />
                    </div>
                </div>
            </div>

            <div className="rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/30 px-3 py-3">
                <label className="flex items-center justify-between gap-3 cursor-pointer">
                    <span className="text-sm font-medium text-slate-800 dark:text-slate-200">Out of stock only</span>
                    <button
                        type="button"
                        role="switch"
                        aria-checked={outOfStockOnly}
                        onClick={() => onOutOfStockOnlyChange(!outOfStockOnly)}
                        className={`relative inline-flex h-7 w-12 shrink-0 rounded-full border-2 border-transparent transition-colors focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 dark:focus:ring-offset-slate-900 ${
                            outOfStockOnly ? 'bg-indigo-600' : 'bg-slate-200 dark:bg-slate-600'
                        }`}
                    >
                        <span
                            className={`pointer-events-none inline-block h-6 w-6 transform rounded-full bg-white shadow ring-0 transition ${
                                outOfStockOnly ? 'translate-x-5' : 'translate-x-0.5'
                            }`}
                        />
                    </button>
                </label>
            </div>

            <div className="grid grid-cols-2 gap-2.5">
                <div>
                    <label className={label} htmlFor="sort-by">Sort by</label>
                    <select
                        id="vendor-sort-by"
                        value={sortBy}
                        onChange={(e) => onSortByChange(e.target.value)}
                        className={input}
                    >
                        <option value="createdAt">Newest</option>
                        <option value="price">Price</option>
                        <option value="title">Title</option>
                    </select>
                </div>
                <div>
                    <label className={label} htmlFor="sort-dir">Direction</label>
                    <select
                        id="vendor-sort-dir"
                        value={sortDir}
                        onChange={(e) => onSortDirChange(e.target.value)}
                        className={input}
                    >
                        <option value="desc">Descending</option>
                        <option value="asc">Ascending</option>
                    </select>
                </div>
            </div>
        </div>
    );
}
