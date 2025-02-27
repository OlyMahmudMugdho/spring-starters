import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import Public from './pages/Public.jsx'


const router = createBrowserRouter([
    {
        path: '/',
        element: <App />
    },
    {
        path: '/public',
        element: <Public />
    }
])

createRoot(document.getElementById('root')).render(
    <RouterProvider router={router} />
)