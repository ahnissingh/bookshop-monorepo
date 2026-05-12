/**
 * Extract a user-facing message from Axios / Spring error responses.
 */
export function getApiErrorMessage(error, fallback = 'Something went wrong') {
    const d = error.response?.data;
    if (!d) return fallback;
    if (typeof d.message === 'string' && d.message.length > 0) return d.message;
    if (typeof d.detail === 'string' && d.detail.length > 0) return d.detail;
    if (typeof d.error === 'string' && d.error.length > 0) return d.error;
    return fallback;
}

/**
 * Detect UnverifiedAccountException-style responses (/403) from registration.
 */
export function isUnverifiedAccountError(error) {
    const data = error.response?.data;
    return data?.error === 'UNVERIFIED_ACCOUNT' || data?.errorCode === 'UNVERIFIED_ACCOUNT';
}