import api from './axiosClient';

/**
 * Build query string for Spring @RequestParam List<String> grades
 * (repeated keys: grades=A&grades=B).
 */
export function buildClientBookSearchParams(filters) {
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
    if (filters.inStockOnly === true) {
        params.set('inStockOnly', 'true');
    }

    const grades = filters.grades ?? [];
    grades.forEach((g) => {
        if (g) params.append('grades', g);
    });

    return params;
}

export const getClientBooks = (filters) => {
    const params = buildClientBookSearchParams(filters);
    const qs = params.toString();
    return api.get(qs ? `/client/books?${qs}` : '/client/books');
};

export const getClientBookById = (id) =>
    api.get(`/client/books/${id}`);
