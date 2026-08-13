import api from "./axios";

import type { AccountResponse,
  AccountSummaryResponse,
  CreateAccountRequest,
  UpdateAccountRequest,
  TransactionResponse,
} from "@/types/types";

export const accountsApi = {
  getAll: () => api.get<AccountSummaryResponse>("/accounts").then((r) => r.data),

  get: (id: string) => api.get<AccountResponse>(`/accounts/${id}`).then((r) => r.data),

  create: (body: CreateAccountRequest) =>
    api.post<AccountResponse>("/accounts", body).then((r) => r.data),

  update: (id: string, body: UpdateAccountRequest) =>
    api.put<AccountResponse>(`/accounts/${id}`, body).then((r) => r.data),

  remove: (id: string) => api.delete(`/accounts/${id}`),

  getTransactions: (id: string) =>
    api.get<TransactionResponse[]>(`/accounts/${id}/transactions`).then((r) => r.data),
};
