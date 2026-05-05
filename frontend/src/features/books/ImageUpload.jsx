import { useState, useRef } from 'react';
import Modal from '../../components/ui/Modal';

export default function ImageUpload({ isOpen, onClose, onUpload, book }) {
    const [file, setFile] = useState(null);
    const [preview, setPreview] = useState(null);
    const [uploading, setUploading] = useState(false);
    const inputRef = useRef(null);

    const handleFile = (f) => {
        if (!f) return;
        setFile(f);
        const reader = new FileReader();
        reader.onloadend = () => setPreview(reader.result);
        reader.readAsDataURL(f);
    };

    const handleDrop = (e) => {
        e.preventDefault();
        const f = e.dataTransfer.files[0];
        if (f && f.type.startsWith('image/')) handleFile(f);
    };

    const handleSubmit = async () => {
        if (!file) return;
        setUploading(true);
        try {
            await onUpload(book.id, file);
            setFile(null);
            setPreview(null);
            onClose();
        } finally {
            setUploading(false);
        }
    };

    const handleClose = () => {
        setFile(null);
        setPreview(null);
        onClose();
    };

    return (
        <Modal isOpen={isOpen} onClose={handleClose} title={`Upload Image — ${book?.title || ''}`}>
            <div
                onDragOver={(e) => e.preventDefault()}
                onDrop={handleDrop}
                onClick={() => inputRef.current?.click()}
                className="border-2 border-dashed border-slate-200 dark:border-slate-700 rounded-xl p-8 text-center cursor-pointer hover:border-slate-400 dark:hover:border-slate-500 transition-colors"
            >
                {preview ? (
                    <img src={preview} alt="Preview" className="max-h-48 mx-auto rounded-lg object-contain" />
                ) : (
                    <div className="space-y-2">
                        <svg className="w-10 h-10 mx-auto text-slate-300 dark:text-slate-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                            <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
                        </svg>
                        <p className="text-sm text-slate-500 dark:text-slate-400">Drop an image here or click to browse</p>
                        <p className="text-xs text-slate-400 dark:text-slate-500">PNG, JPG up to 5MB</p>
                    </div>
                )}
                <input
                    ref={inputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={(e) => handleFile(e.target.files[0])}
                />
            </div>

            {file && (
                <div className="flex items-center justify-between mt-4">
                    <span className="text-sm text-slate-500 dark:text-slate-400 truncate max-w-[200px]">{file.name}</span>
                    <div className="flex gap-2">
                        <button
                            onClick={() => { setFile(null); setPreview(null); }}
                            className="px-3 py-1.5 rounded-lg text-sm font-medium text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                        >
                            Clear
                        </button>
                        <button
                            onClick={handleSubmit}
                            disabled={uploading}
                            className="px-4 py-1.5 rounded-lg text-sm font-medium bg-slate-900 dark:bg-slate-50 text-white dark:text-slate-900 hover:bg-slate-800 dark:hover:bg-slate-200 disabled:opacity-50 transition-colors"
                        >
                            {uploading ? 'Uploading...' : 'Upload'}
                        </button>
                    </div>
                </div>
            )}
        </Modal>
    );
}
