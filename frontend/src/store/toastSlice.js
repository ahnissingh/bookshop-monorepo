import { createSlice } from '@reduxjs/toolkit';

let nextId = 0;

const toastSlice = createSlice({
    name: 'toast',
    initialState: {
        toasts: [],
    },
    reducers: {
        addToastInternal: (state, action) => {
            state.toasts.push(action.payload);
        },
        setToastExiting: (state, action) => {
            const toast = state.toasts.find(t => t.id === action.payload);
            if (toast) {
                toast.exiting = true;
            }
        },
        removeToast: (state, action) => {
            state.toasts = state.toasts.filter(t => t.id !== action.payload);
        }
    },
});

export const { addToastInternal, setToastExiting, removeToast } = toastSlice.actions;

export const addToast = (message, type = 'info', duration = 4000) => (dispatch) => {
    const id = ++nextId;
    dispatch(addToastInternal({ id, message, type, exiting: false }));
    
    setTimeout(() => {
        dispatch(setToastExiting(id));
        setTimeout(() => {
            dispatch(removeToast(id));
        }, 300);
    }, duration);
};

export const dismissToast = (id) => (dispatch) => {
    dispatch(setToastExiting(id));
    setTimeout(() => {
        dispatch(removeToast(id));
    }, 300);
};

export default toastSlice.reducer;
