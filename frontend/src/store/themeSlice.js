import { createSlice } from '@reduxjs/toolkit';

const getInitialTheme = () => {
    const saved = localStorage.getItem('theme');
    if (saved) return saved;
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
};

const themeSlice = createSlice({
    name: 'theme',
    initialState: {
        mode: getInitialTheme(),
    },
    reducers: {
        setThemeMode: (state, action) => {
            state.mode = action.payload;
        }
    },
});

export const { setThemeMode } = themeSlice.actions;

export const toggleTheme = () => (dispatch, getState) => {
    const currentMode = getState().theme.mode;
    const newMode = currentMode === 'dark' ? 'light' : 'dark';
    
    localStorage.setItem('theme', newMode);
    if (newMode === 'dark') {
        document.documentElement.classList.add('dark');
    } else {
        document.documentElement.classList.remove('dark');
    }
    
    dispatch(setThemeMode(newMode));
};

export const initTheme = () => (dispatch, getState) => {
    const currentMode = getState().theme.mode;
    if (currentMode === 'dark') {
        document.documentElement.classList.add('dark');
    } else {
        document.documentElement.classList.remove('dark');
    }
};

export default themeSlice.reducer;
