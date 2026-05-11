import api from './axios'

export const accountsApi = {
  getAll: () => api.get('/api/accounts'),
  getOne: (id) => api.get(`/api/accounts/${id}`),
  create: (data) => api.post('/api/accounts', data),
  update: (id, data) => api.put(`/api/accounts/${id}`, data),
  delete: (id) => api.delete(`/api/accounts/${id}`),
  getTransactions: (id) => api.get(`/api/accounts/${id}/transactions`),
  updateTransaction: (accountId, txId, data) => api.put(`/api/accounts/${accountId}/transactions/${txId}`, data),
  deleteTransaction: (accountId, txId) => api.delete(`/api/accounts/${accountId}/transactions/${txId}`),
  getTransfers: () => api.get('/api/transfers'),
  confirmTransfer: (id) => api.put(`/api/transfers/${id}/confirm`),
  dismissTransfer: (id) => api.put(`/api/transfers/${id}/dismiss`),
}