import api from './axiosClient';

const multipartConfig = {
    headers: { 'Content-Type': undefined },
};

/**
 * @param {Record<string, unknown>} formValues
 */
export function mapFormValuesToBookRequest(formValues) {
    const subtitle = formValues.subtitle?.trim?.() ?? formValues.subtitle;
    const description = formValues.description?.trim?.() ?? formValues.description;

    return {
        title: formValues.title,
        author: formValues.author,
        subtitle: subtitle || null,
        price: Number(formValues.price),
        grade: formValues.grade || null,
        description: description || null,
        quantity: parseInt(String(formValues.quantity), 10),
    };
}

/**
 * @param {object} bookData
 * @param {File} [file]
 */
export function buildVendorBookFormData(bookData, file) {
    const formData = new FormData();
    const jsonBlob = new Blob([JSON.stringify(bookData)], { type: 'application/json' });
    formData.append('book', jsonBlob);
    if (file) {
        formData.append('file', file);
    }
    return formData;
}

/**
 * Build query string for Spring VendorBookFilterRequest + page/sort params.
 */
export function buildVendorBookSearchParams(filters) {
    const params = new URLSearchParams();

    const page = filters.page ?? 0;
    const size = filters.size ?? 12;
    const sortBy = filters.sortBy ?? 'createdAt';
    const sortDir = filters.sortDir ?? 'desc';

    params.set('page', String(page));
    params.set('size', String(size));
    params.set('sortBy', sortBy);
    params.set('sortDir', sortDir);

    if (filters.search != null && String(filters.search).trim() !== '') {
        params.set('search', String(filters.search).trim());
    }
    if (filters.minPrice !== '' && filters.minPrice != null && !Number.isNaN(Number(filters.minPrice))) {
        params.set('minPrice', String(filters.minPrice));
    }
    if (filters.maxPrice !== '' && filters.maxPrice != null && !Number.isNaN(Number(filters.maxPrice))) {
        params.set('maxPrice', String(filters.maxPrice));
    }
    if (filters.outOfStockOnly === true) {
        params.set('outOfStockOnly', 'true');
    }

    const grades = filters.grades ?? [];
    grades.forEach((g) => {
        if (g) params.append('grades', g);
    });

    return params;
}

export const getMyBooks = (filters) => {
    const qs = buildVendorBookSearchParams(filters).toString();
    return api.get(qs ? `/vendor/books?${qs}` : '/vendor/books');
};

export const getBookById = (id) =>
    api.get(`/vendor/books/${id}`);

export const createBook = (formData) =>
    api.post('/vendor/books', formData, multipartConfig);

export const updateBook = (id, formData) =>
    api.put(`/vendor/books/${id}`, formData, multipartConfig);

export const deleteBook = (id) =>
    api.delete(`/vendor/books/${id}`);
