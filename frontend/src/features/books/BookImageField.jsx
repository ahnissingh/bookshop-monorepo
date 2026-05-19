import { useRef, useEffect } from 'react';
import { useBookImageFile } from '../../hooks/useBookImageFile';

export default function BookImageField({ existingImageUrl, onFileChange, onCompressingChange, disabled }) {
    const inputRef = useRef(null);
    const {
        file,
        displayUrl,
        isCompressing,
        error,
        sizeLabel,
        hasNewFile,
        processFile,
        clearImage,
    } = useBookImageFile(existingImageUrl);

    useEffect(() => {
        onFileChange?.(file);
    }, [file, onFileChange]);

    useEffect(() => {
        onCompressingChange?.(isCompressing);
    }, [isCompressing, onCompressingChange]);

    const handleFile = async (raw) => {
        if (!raw) return;
        await processFile(raw);
    };

    const handleClear = () => {
        clearImage();
        if (inputRef.current) inputRef.current.value = '';
    };

    const handleDrop = (e) => {
        e.preventDefault();
        const f = e.dataTransfer.files?.[0];
        if (f?.type?.startsWith('image/')) handleFile(f);
    };

    const showExistingOnly = displayUrl && !hasNewFile && !!existingImageUrl;

    return (
        <div className="space-y-3">
            <div>
                <p className="text-sm font-medium text-slate-700 dark:text-slate-300">Cover image</p>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">Optional. Optimized to ~250KB before upload.</p>
            </div>
            <div
                role="button"
                tabIndex={0}
                onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        if (!disabled && !isCompressing) inputRef.current?.click();
                    }
                }}
                onDragOver={(e) => e.preventDefault()}
                onDrop={handleDrop}
                onClick={() => !disabled && !isCompressing && inputRef.current?.click()}
                className={`relative rounded-xl border-2 border-dashed transition-colors overflow-hidden ${
                    disabled || isCompressing
                        ? 'opacity-60 cursor-not-allowed'
                        : 'cursor-pointer hover:border-indigo-400 dark:hover:border-indigo-500'
                } border-slate-200 dark:border-slate-700 bg-slate-50/80 dark:bg-slate-800/40`}
            >
                {displayUrl ? (
                    <div className="aspect-[4/3] flex items-center justify-center p-3 bg-white dark:bg-slate-900">
                        <img
                            src={displayUrl}
                            alt="Book cover preview"
                            className="max-h-full max-w-full object-contain rounded-lg"
                        />
                    </div>
                ) : (
                    <div className="aspect-[4/3] flex flex-col items-center justify-center gap-2 p-6 text-center">
                        <svg className="w-10 h-10 text-slate-300 dark:text-slate-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.41a2.25 2.25 0 013.182 0l2.909 2.91m-18 3.75h16.5a1.5 1.5 0 001.5-1.5V6a1.5 1.5 0 00-1.5-1.5H3.75A1.5 1.5 0 002.25 6v12a1.5 1.5 0 001.5 1.5zm10.5-11.25h.008v.008h-.008V8.25zm.375 0a.375.375 0 11-.75 0 .375.375 0 01.75 0z" />
                        </svg>
                        <p className="text-sm text-slate-500 dark:text-slate-400">Drop image or click to browse</p>
                    </div>
                )}
                {isCompressing && (
                    <div className="absolute inset-0 flex items-center justify-center bg-white/80 dark:bg-slate-900/80 backdrop-blur-sm">
                        <span className="text-sm font-medium text-slate-600 dark:text-slate-300">Optimizing image…</span>
                    </div>
                )}
                <input
                    ref={inputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    disabled={disabled || isCompressing}
                    onChange={(e) => handleFile(e.target.files?.[0])}
                />
            </div>
            {(sizeLabel || showExistingOnly) && !isCompressing && (
                <p className="text-xs text-slate-500 dark:text-slate-400">
                    {hasNewFile && sizeLabel
                        ? `Ready to upload (${sizeLabel})`
                        : showExistingOnly
                          ? 'Current cover (upload a new file to replace)'
                          : null}
                </p>
            )}
            {error && <p className="text-xs text-red-500 dark:text-red-400">{error}</p>}
            {(displayUrl || hasNewFile) && (
                <button
                    type="button"
                    onClick={(e) => {
                        e.stopPropagation();
                        handleClear();
                    }}
                    disabled={disabled || isCompressing}
                    className="text-sm font-medium text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 disabled:opacity-50"
                >
                    {hasNewFile ? 'Remove new image' : 'Clear selection'}
                </button>
            )}
        </div>
    );
}
