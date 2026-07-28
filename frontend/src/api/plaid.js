import api from './axios'

export const plaidApi = {
  getLinkToken: () => api.post('/api/plaid/link-token'),
  exchangePublicToken: (publicToken) =>
    api.post('/api/plaid/exchange-public-token', { publicToken }),
  getItems: () => api.get('/api/plaid/items'),
  sync: (itemId) => api.post(`/api/plaid/items/${itemId}/sync`),
  removeItem: (itemId) => api.delete(`/api/plaid/items/${itemId}`),
}