import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "@/pages/HomePage";
import DebtPage from "@/pages/DebtPage";
import BudgetPage from "@/pages/BudgetPage";
import LoginPage from "@/pages/LoginPage";
import SignupPage from "@/pages/SignupPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/debt" element={<DebtPage />} />
          <Route path="/budget" element={<BudgetPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
