import { createSlice } from '@reduxjs/toolkit';

const initialState = {
    username: null,
    roles: [],
    isAuthenticated: false,
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setCredentials: (state, action) => {
            const { username, roles } = action.payload;
            state.username = username;
            state.roles = roles;
            state.isAuthenticated = true;
        },
        logout: (state) => {
            state.username = null;
            state.roles = [];
            state.isAuthenticated = false;
        },
    },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;