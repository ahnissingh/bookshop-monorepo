import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice';
import booksReducer from './booksSlice';
import clientBooksReducer from './clientBooksSlice';
import themeReducer from './themeSlice';
import toastReducer from './toastSlice';

export const store = configureStore({
    reducer: {
        auth: authReducer,
        books: booksReducer,
        clientBooks: clientBooksReducer,
        theme: themeReducer,
        toast: toastReducer,
    },
});