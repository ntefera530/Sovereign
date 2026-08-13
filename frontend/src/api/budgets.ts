import api from "./axios";

import type {
  BudgetResponse,
  BudgetSummaryResponse,
  BudgetCategoryResponse,
  CreateBudgetRequest,
  UpdateBudgetRequest,
  CreateCategoryRequest,
  UpdateCategoryRequest,
} from "@/types/types";

export const budgetsApi = {
  getAll: () => api.get<BudgetResponse[]>("/budgets").then((r) => r.data),

  get: (id: string) => api.get<BudgetResponse>(`/budgets/${id}`).then((r) => r.data),

  create: (body: CreateBudgetRequest) =>
    api.post<BudgetResponse>("/budgets", body).then((r) => r.data),

  update: (id: string, body: UpdateBudgetRequest) =>
    api.put<BudgetResponse>(`/budgets/${id}`, body).then((r) => r.data),

  remove: (id: string) => api.delete(`/budgets/${id}`),

  getSummary: (id: string) =>
    api.get<BudgetSummaryResponse>(`/budgets/${id}/summary`).then((r) => r.data),

  getCategories: (id: string) =>
    api.get<BudgetCategoryResponse[]>(`/budgets/${id}/categories`).then((r) => r.data),

  createCategory: (id: string, body: CreateCategoryRequest) =>
    api.post<BudgetCategoryResponse>(`/budgets/${id}/categories`, body).then((r) => r.data),

  updateCategory: (id: string, catId: string, body: UpdateCategoryRequest) =>
    api
      .put<BudgetCategoryResponse>(`/budgets/${id}/categories/${catId}`, body)
      .then((r) => r.data),

  deleteCategory: (id: string, catId: string) =>
    api.delete(`/budgets/${id}/categories/${catId}`),
};
