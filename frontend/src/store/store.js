import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import booksReducer from './booksSlice';
import themeReducer from './themeSlice';
import toastReducer from './toastSlice';

export const store = configureStore({
    reducer: {
        auth: authReducer,
        books: booksReducer,
        theme: themeReducer,
        toast: toastReducer,
    },
});