import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as bookApi from '../api/bookApi';

export const fetchBooks = createAsyncThunk(
    'books/fetchBooks',
    async ({ page = 0, size = 10, sort = 'createdAt,desc' }, { rejectWithValue }) => {
        try {
            const response = await bookApi.getMyBooks(page, size, sort);
            return response.data.data;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || 'Failed to fetch books');
        }
    }
);

export const createBookThunk = createAsyncThunk(
    'books/createBook',
    async (data, { rejectWithValue }) => {
        try {
            const response = await bookApi.createBook(data);
            return response.data.data;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || 'Failed to create book');
        }
    }
);

export const updateBookThunk = createAsyncThunk(
    'books/updateBook',
    async ({ id, data }, { rejectWithValue }) => {
        try {
            const response = await bookApi.updateBook(id, data);
            return response.data.data;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || 'Failed to update book');
        }
    }
);

export const deleteBookThunk = createAsyncThunk(
    'books/deleteBook',
    async (id, { rejectWithValue }) => {
        try {
            await bookApi.deleteBook(id);
            return id;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || 'Failed to delete book');
        }
    }
);

export const uploadImageThunk = createAsyncThunk(
    'books/uploadImage',
    async ({ id, file }, { rejectWithValue }) => {
        try {
            await bookApi.uploadBookImage(id, file);
            return id;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || 'Failed to upload image');
        }
    }
);

const booksSlice = createSlice({
    name: 'books',
    initialState: {
        items: [],
        totalPages: 0,
        totalElements: 0,
        currentPage: 0,
        pageSize: 10,
        isFirst: true,
        isLast: true,
        loading: 'idle',
        error: null,
    },
    reducers: {
        clearError: (state) => {
            state.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchBooks.pending, (state) => {
                state.loading = 'pending';
                state.error = null;
            })
            .addCase(fetchBooks.fulfilled, (state, action) => {
                state.loading = 'succeeded';
                state.items = action.payload.content;
                state.totalPages = action.payload.totalPages;
                state.totalElements = action.payload.totalElements;
                state.currentPage = action.payload.number;
                state.pageSize = action.payload.size;
                state.isFirst = action.payload.first;
                state.isLast = action.payload.last;
            })
            .addCase(fetchBooks.rejected, (state, action) => {
                state.loading = 'failed';
                state.error = action.payload;
            })
            .addCase(createBookThunk.fulfilled, (state, action) => {
                state.items.unshift(action.payload);
                state.totalElements += 1;
            })
            .addCase(updateBookThunk.fulfilled, (state, action) => {
                const idx = state.items.findIndex((b) => b.id === action.payload.id);
                if (idx !== -1) state.items[idx] = action.payload;
            })
            .addCase(deleteBookThunk.fulfilled, (state, action) => {
                state.items = state.items.filter((b) => b.id !== action.payload);
                state.totalElements -= 1;
            });
    },
});

export const { clearError } = booksSlice.actions;
export default booksSlice.reducer;
