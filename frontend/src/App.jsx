import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import Dashboard from './pages/dashboard/Dashboard'
import Accounts from './pages/accounts/Accounts'
import Debts from './pages/debt/Debts'
import Budgets from './pages/budget/Budgets'
import NetWorth from './pages/networth/NetWorth'
import BillSplitter from './pages/bills/BillSplitter'
import Settings from './pages/settings/Settings'
import Layout from './components/layout/Layout'

function PrivateRoute({ children }) {
  const { user, isLoading } = useAuth()
  if (isLoading) return <div>Loading...</div>
  return user ? children : <Navigate to="/login" />
}

function PublicRoute({ children }) {
  const { user, isLoading } = useAuth()
  if (isLoading) return <div>Loading...</div>
  return user ? <Navigate to="/dashboard" /> : children
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={
        <PublicRoute><Login /></PublicRoute>
      } />
      <Route path="/register" element={
        <PublicRoute><Register /></PublicRoute>
      } />
      <Route path="/" element={
        <PrivateRoute><Layout /></PrivateRoute>
      }>
        <Route index element={<Navigate to="/dashboard" />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="accounts" element={<Accounts />} />
        <Route path="debts" element={<Debts />} />
        <Route path="budgets" element={<Budgets />} />
        <Route path="networth" element={<NetWorth />} />
        <Route path="bills" element={<BillSplitter />} />
        <Route path="settings" element={<Settings />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" />} />
    </Routes>
  )
}