
export type AccountType =
  | "CHECKING"
  | "SAVINGS"
  | "CREDIT_CARD"
  | "INVESTMENT"
  | "LOAN"
  | "CASH"
  | "OTHER";

export type DebtType =
  | "MORTGAGE"
  | "AUTO_LOAN"
  | "STUDENT_LOAN"
  | "CREDIT_CARD"
  | "PERSONAL_LOAN"
  | "MEDICAL"
  | "OTHER";

export type PaymentFrequency = "WEEKLY" | "BIWEEKLY" | "MONTHLY" | "QUARTERLY" | "YEARLY";

export type BudgetPeriodType = "WEEKLY" | "BIWEEKLY" | "MONTHLY" | "QUARTERLY" | "YEARLY" | "CUSTOM";

export type AssetType = "REAL_ESTATE" | "VEHICLE" | "INVESTMENT" | "COLLECTIBLE" | "BUSINESS" | "OTHER";

export type NetWorthCalculationType = "TOTAL" | "LIQUID" | "CASH_ONLY" | "EXCLUDING_REAL_ESTATE" | "CUSTOM";

export type SyncStatus = "SUCCESS" | "FAILED" | "PARTIAL" | "IN_PROGRESS";

// ── Accounts ─────────────────────────────────────────────────────

export interface AccountResponse {
  id: string;
  name: string;
  type: AccountType;
  balance: number;
  currency: string;
  isActive: boolean;
  createdAt: string;
}

export interface AccountSummaryResponse {
  totalBalance: number;
  totalAccounts: number;
  accounts: AccountResponse[];
}

export interface CreateAccountRequest {
  name: string;
  type: AccountType;
  initialBalance: number;
  currency: string;
}

export interface UpdateAccountRequest {
  name: string;
  isActive: boolean;
}

export interface TransactionResponse {
  id: string;
  date: string;
  name: string;
  amount: number;
  category?: string;
  pending?: boolean;
}

// ── Debts ────────────────────────────────────────────────────────

export interface DebtResponse {
  id: string;
  name: string;
  debtType: DebtType;
  principal: number;
  currentBalance: number;
  interestRate: number; // decimal, e.g. 0.2499
  minimumPayment: number;
  paymentFrequency: PaymentFrequency;
  dueDate: string;
  isPaidOff: boolean;
  dailyInterestCost: number;
  monthlyInterestCost: number;
  percentagePaidOff: number;
  createdAt: string;
}

export interface DebtSummaryResponse {
  totalDebt: number;
  totalMinimumPayments: number;
  totalMonthlyInterest: number;
  totalDailyInterest: number;
  estimatedDebtFreeDate: string | null;
  monthsToDebtFree: number;
  totalInterestIfMinimumsOnly: number;
  debts: DebtResponse[];
}

export interface CreateDebtRequest {
  name: string;
  debtType: DebtType;
  principal: number;
  currentBalance: number;
  interestRate: number;
  minimumPayment: number;
  paymentFrequency: PaymentFrequency;
  dueDate: string;
}

export interface UpdateDebtRequest {
  name: string;
  currentBalance: number;
  interestRate: number;
  minimumPayment: number;
  paymentFrequency: PaymentFrequency;
  dueDate: string;
}

export interface AmortizationEntry {
  monthNumber: number;
  paymentDate: string;
  payment: number;
  principal: number;
  interest: number;
  remainingBalance: number;
}

export interface PayoffProjectionResponse {
  debtId: string;
  debtName: string;
  currentBalance: number;
  interestRate: number;
  monthlyPayment: number;
  payoffDate: string;
  monthsToPayoff: number;
  totalInterestPaid: number;
  totalAmountPaid: number;
  interestToBalanceRatio: number;
  schedule: AmortizationEntry[];
}

export interface SimulationResponse {
  debtId: string;
  debtName: string;
  baselinePayoffDate: string;
  baselineMonths: number;
  baselineTotalInterest: number;
  simulatedPayoffDate: string;
  simulatedMonths: number;
  simulatedTotalInterest: number;
  monthsSaved: number;
  interestSaved: number;
  extraMonthlyPayment: number;
  simulatedSchedule: AmortizationEntry[];
}

export interface StrategyResult {
  order: { debtId: string; debtName: string; payoffOrder: number; payoffDate: string; totalInterestPaid: number }[];
  totalMonths: number;
  totalInterestPaid: number;
  debtFreeDate: string;
}

export interface StrategyComparisonResponse {
  extraMonthlyPayment: number;
  totalDebt: number;
  avalanche: StrategyResult;
  snowball: StrategyResult;
  custom: StrategyResult | null;
  recommendation: string;
}

// ── Budgets ──────────────────────────────────────────────────────

export interface BudgetCategoryResponse {
  id: string;
  name: string;
  limitAmount: number;
  spentAmount: number;
  remainingAmount: number;
  percentageUsed: number;
  isOverBudget: boolean;
  isApproachingLimit: boolean;
  createdAt: string;
}

export interface BudgetResponse {
  id: string;
  name: string;
  periodType: BudgetPeriodType;
  startDate: string;
  endDate: string | null;
  totalLimit: number;
  totalSpent: number;
  totalRemaining: number;
  isActive: boolean;
  categories: BudgetCategoryResponse[];
  createdAt: string;
}

export interface BudgetSummaryResponse {
  totalLimit: number;
  totalSpent: number;
  totalRemaining: number;
  percentageUsed: number;
  categoriesOverBudget: number;
  categoriesApproachingLimit: number;
  categories: BudgetCategoryResponse[];
  insight: string;
}

export interface CreateBudgetRequest {
  name: string;
  periodType: BudgetPeriodType;
  startDate: string;
  endDate?: string | null;
}

export interface UpdateBudgetRequest {
  name: string;
  endDate?: string | null;
}

export interface CreateCategoryRequest {
  name: string;
  limitAmount: number;
}

export interface UpdateCategoryRequest {
  name: string;
  limitAmount: number;
}

// ── Net worth ────────────────────────────────────────────────────

export interface AssetBreakdown {
  sourceId: string;
  sourceType: string;
  name: string;
  value: number;
}

export interface NetWorthResponse {
  calculationType: NetWorthCalculationType;
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
  liquidAssets: number;
  cashOnHand: number;
  assetBreakdown: AssetBreakdown[];
  liabilityBreakdown: AssetBreakdown[];
  calculatedAt: string;
}

export interface NetWorthSnapshotResponse {
  id: string;
  calculationType: NetWorthCalculationType;
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
  snapshotDate: string;
}

export interface NetWorthHistoryResponse {
  snapshots: NetWorthSnapshotResponse[];
  changeFromFirst: number;
  changeFromLast: number;
  highestNetWorth: number;
  lowestNetWorth: number;
  trend: string;
}

export interface ManualAssetResponse {
  id: string;
  name: string;
  type: AssetType;
  value: number;
  currency: string;
  valuationDate: string;
  notes: string | null;
  createdAt: string;
}

export interface CreateAssetRequest {
  name: string;
  type: AssetType;
  value: number;
  currency: string;
  valuationDate: string;
  notes?: string;
}

export type UpdateAssetRequest = CreateAssetRequest;

// ── Plaid ────────────────────────────────────────────────────────

export interface LinkTokenResponse {
  linkToken: string;
}

export interface PlaidItemResponse {
  id: string;
  institutionName: string;
  syncStatus: SyncStatus;
  lastSyncedAt: string | null;
  accountCount: number;
}
