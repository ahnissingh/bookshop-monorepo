import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { Provider } from 'react-redux';
import { store } from './store/store';
import { initTheme } from './store/themeSlice';
import ToastContainer from './components/ui/Toast';

store.dispatch(initTheme());
import './index.css';

import Layout from './components/Layout.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import PersistLogin from './components/PersistLogin.jsx';
import DashboardLayout from './layouts/DashboardLayout.jsx';
import BooksPage from './pages/vendor/BooksPage.jsx';
import AddBookPage from './pages/vendor/AddBookPage.jsx';
import EditBookPage from './pages/vendor/EditBookPage.jsx';

const router = createBrowserRouter([
    {
        element: <PersistLogin />,
        children: [
            {
                path: "/",
                element: <Layout />,
                children: [
                    { index: true, element: <Home /> },
                    { path: "login", element: <Login /> },
                    { path: "register", element: <Register /> },
                ],
            },
            {
                element: <ProtectedRoute allowedRoles={['ROLE_VENDOR']} />,
                children: [
                    {
                        path: "/dashboard",
                        element: <DashboardLayout />,
                        children: [
                            { index: true, element: <BooksPage /> },
                            { path: "add", element: <AddBookPage /> },
                            { path: "edit/:id", element: <EditBookPage /> },
                        ],
                    },
                ],
            },
        ],
    },
]);

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <Provider store={store}>
            <RouterProvider router={router} />
            <ToastContainer />
        </Provider>
    </StrictMode>,
);