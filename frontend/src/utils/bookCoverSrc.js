
export function bookCoverSrc(pictureUrl, updatedAt) {
    if (!pictureUrl) return null;
    if (!updatedAt) return pictureUrl;
    const t = encodeURIComponent(String(updatedAt));
    const sep = pictureUrl.includes('?') ? '&' : '?';
    return `${pictureUrl}${sep}t=${t}`;
}

export function bookCoverSrcFromBook(book) {
    return bookCoverSrc(book?.pictureUrl, book?.updatedAt);
}
