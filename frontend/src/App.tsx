import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "@/pages/HomePage";
import DebtPage from "@/pages/DebtPage";
import BudgetPage from "@/pages/BudgetPage";
import LoginPage from "@/pages/LoginPage";
import SignupPage from "@/pages/SignupPage";
import DashboardPage from "@/pages/DashboardPage";
import Layout from "./components/Layout";
import { AccountsProvider } from "./contexts/AccountContext";

function App() {
  return (
    <BrowserRouter>
      <AccountsProvider>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/debt" element={<DebtPage />} />
            <Route path="/budget" element={<BudgetPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>
        </Routes>
      </AccountsProvider>      
    </BrowserRouter>
  )
}

export default App
