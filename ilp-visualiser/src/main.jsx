import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from "react-router-dom"

import Planner from "./pages/Planner.jsx"
import Visualiser from "./pages/Visualiser.jsx"

import './index.css'

ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Planner />} />
                <Route path="/visualiser" element={<Visualiser />} />
            </Routes>
        </BrowserRouter>
    </React.StrictMode>
)
