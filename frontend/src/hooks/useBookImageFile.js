import { useState, useEffect, useCallback, useRef } from 'react';
import { compressBookImage } from '../utils/compressBookImage';

/**
 * Manages book cover selection, compression, and preview URL lifecycle.
 * @param {string | null | undefined} [existingImageUrl]
 */
export function useBookImageFile(existingImageUrl) {
    const [file, setFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);
    const [isCompressing, setIsCompressing] = useState(false);
    const [error, setError] = useState(null);
    const [sizeLabel, setSizeLabel] = useState(null);
    const blobUrlRef = useRef(null);

    const revokeBlobUrl = useCallback(() => {
        if (blobUrlRef.current) {
            URL.revokeObjectURL(blobUrlRef.current);
            blobUrlRef.current = null;
        }
    }, []);

    useEffect(() => () => revokeBlobUrl(), [revokeBlobUrl]);

    const displayUrl = previewUrl || existingImageUrl || null;

    const formatSize = (bytes) => {
        if (bytes < 1024) return `${bytes} B`;
        if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
        return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
    };

    const processFile = useCallback(
        async (raw) => {
            if (!raw) return;
            setError(null);
            setIsCompressing(true);
            try {
                const compressed = await compressBookImage(raw);
                revokeBlobUrl();
                const url = URL.createObjectURL(compressed);
                blobUrlRef.current = url;
                setFile(compressed);
                setPreviewUrl(url);
                setSizeLabel(formatSize(compressed.size));
            } catch (e) {
                setError(e.message || 'Could not process this image.');
                setFile(null);
                setPreviewUrl(null);
                setSizeLabel(null);
            } finally {
                setIsCompressing(false);
            }
        },
        [revokeBlobUrl]
    );

    const clearImage = useCallback(() => {
        revokeBlobUrl();
        setFile(null);
        setPreviewUrl(null);
        setSizeLabel(null);
        setError(null);
    }, [revokeBlobUrl]);

    return {
        file,
        displayUrl,
        isCompressing,
        error,
        sizeLabel,
        hasNewFile: !!file,
        processFile,
        clearImage,
    };
}
