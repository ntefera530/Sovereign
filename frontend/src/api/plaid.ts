import api from "./axios";
import type { LinkTokenResponse, PlaidItemResponse } from "@/types/types";

export const plaidApi = {
  createLinkToken: () => api.post<LinkTokenResponse>("/plaid/link-token").then((r) => r.data),

  exchangePublicToken: (publicToken: string) =>
    api
      .post<PlaidItemResponse[]>("/plaid/exchange-public-token", { publicToken })
      .then((r) => r.data),

  getItems: () => api.get<PlaidItemResponse[]>("/plaid/items").then((r) => r.data),

  sync: (itemId: string) => api.post(`/plaid/items/${itemId}/sync`),

  removeItem: (itemId: string) => api.delete(`/plaid/items/${itemId}`),
};

export default plaidApi;