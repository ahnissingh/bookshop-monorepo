import api from './axiosClient';

export const getMyBooks = (page = 0, size = 10) =>
    api.get('/vendor/books', { params: { page, size } });

export const getBookById = (id) =>
    api.get(`/vendor/books/${id}`);

export const createBook = (data) =>
    api.post('/vendor/books', data);

export const updateBook = (id, data) =>
    api.put(`/vendor/books/${id}`, data);

export const deleteBook = (id) =>
    api.delete(`/vendor/books/${id}`);

export const uploadBookImage = (id, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post(`/vendor/books/${id}/picture`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
};
