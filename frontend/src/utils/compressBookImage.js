import imageCompression from 'browser-image-compression';

const MAX_SIZE_MB = 0.25;
const MAX_DIMENSION = 1920;

/**
 * @param {File} file
 * @returns {Promise<File>}
 */
export async function compressBookImage(file) {
    if (!file?.type?.startsWith('image/')) {
        throw new Error('Please choose an image file (PNG, JPG, or similar).');
    }

    const compressed = await imageCompression(file, {
        maxSizeMB: MAX_SIZE_MB,
        maxWidthOrHeight: MAX_DIMENSION,
        useWebWorker: true,
        fileType: 'image/jpeg',
    });

    const baseName = file.name.replace(/\.[^.]+$/, '') || 'book-cover';
    return new File([compressed], `${baseName}.jpg`, {
        type: 'image/jpeg',
        lastModified: Date.now(),
    });
}
