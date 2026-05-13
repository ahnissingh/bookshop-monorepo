import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as clientBookApi from '../api/clientBookApi';

export const fetchClientBooks = createAsyncThunk(
    'clientBooks/fetchClientBooks',
    async (filters, { rejectWithValue }) => {
        try {
            const response = await clientBookApi.getClientBooks(filters);
            return response.data.data;
        } catch (err) {
            return rejectWithValue(err.response?.data?.message || 'Failed to load books');
        }
    }
);

const clientBooksSlice = createSlice({
    name: 'clientBooks',
    initialState: {
        items: [],
        totalPages: 0,
        totalElements: 0,
        currentPage: 0,
        pageSize: 12,
        isFirst: true,
        isLast: true,
        loading: 'idle',
        error: null,
    },
    reducers: {
        clearClientBooksError: (state) => {
            state.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(fetchClientBooks.pending, (state) => {
                state.loading = 'pending';
                state.error = null;
            })
            .addCase(fetchClientBooks.fulfilled, (state, action) => {
                state.loading = 'succeeded';
                const p = action.payload;
                state.items = p.content ?? [];
                state.totalPages = p.totalPages ?? 0;
                state.totalElements = p.totalElements ?? 0;
                state.currentPage = p.number ?? 0;
                state.pageSize = p.size ?? 12;
                state.isFirst = p.first ?? true;
                state.isLast = p.last ?? true;
            })
            .addCase(fetchClientBooks.rejected, (state, action) => {
                state.loading = 'failed';
                state.error = action.payload;
            });
    },
});

export const { clearClientBooksError } = clientBooksSlice.actions;
export default clientBooksSlice.reducer;
