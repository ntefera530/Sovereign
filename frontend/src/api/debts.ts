import api from "./axios";
import type {
  DebtResponse,
  DebtSummaryResponse,
  CreateDebtRequest,
  UpdateDebtRequest,
  PayoffProjectionResponse,
  SimulationResponse,
  StrategyComparisonResponse,
} from "@/types/types";

export const debtsApi = {
  getAll: () => api.get<DebtResponse[]>("/debts").then((r) => r.data),

  getSummary: () => api.get<DebtSummaryResponse>("/debts/summary").then((r) => r.data),

  get: (id: string) => api.get<DebtResponse>(`/debts/${id}`).then((r) => r.data),

  create: (body: CreateDebtRequest) => api.post<DebtResponse>("/debts", body).then((r) => r.data),

  update: (id: string, body: UpdateDebtRequest) =>
    api.put<DebtResponse>(`/debts/${id}`, body).then((r) => r.data),

  remove: (id: string) => api.delete(`/debts/${id}`),

  getProjection: (id: string) =>
    api.get<PayoffProjectionResponse>(`/debts/${id}/projection`).then((r) => r.data),

  simulate: (debtId: string, extraMonthlyPayment: number) =>
    api
      .post<SimulationResponse>("/debts/simulate", { debtId, extraMonthlyPayment })
      .then((r) => r.data),

  compareStrategies: (extraMonthlyPayment: number, customOrder?: string[]) =>
    api
      .post<StrategyComparisonResponse>("/debts/strategies", {
        extraMonthlyPayment,
        customOrder,
      })
      .then((r) => r.data),
};
