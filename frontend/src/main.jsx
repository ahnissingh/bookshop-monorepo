import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { Provider } from 'react-redux';
import { store } from './store/store';
import './index.css';

import Layout from './components/Layout.jsx';
import Home from './pages/Home.jsx';
import Dashboard from './pages/Dashboard.jsx';

import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';

import ProtectedRoute from './components/ProtectedRoute.jsx';
import PersistLogin from './components/PersistLogin.jsx';



const router = createBrowserRouter([
    {
        // Wrap everything inside PersistLogin
        element: <PersistLogin />,
        children: [
            {
                path: "/",
                element: <Layout/>,
                children: [
                    { path: "/", element: <Home/> },
                    { path: "/login", element: <Login/> },
                    { path: "/register", element: <Register/> },
                    {
                        element: <ProtectedRoute allowedRoles={['ROLE_VENDOR']} />,
                        children: [
                            { path: "/dashboard", element: <Dashboard/> },
                        ]
                    }
                ],
            }
        ]
    },
]);
createRoot(document.getElementById('root')).render(
    <StrictMode>
        <Provider store={store}>
            <RouterProvider router={router} />
        </Provider>
    </StrictMode>,
);