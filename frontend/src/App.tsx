import { BrowserRouter, Routes, Route } from "react-router-dom";
import HomePage from "@/pages/HomePage";
import DebtPage from "@/pages/DebtPage";
import BudgetPage from "@/pages/BudgetPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/debt" element={<DebtPage />} />
          <Route path="/budget" element={<BudgetPage />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
