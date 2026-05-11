import api from './axios'

export const debtsApi = {
  getAll: () => api.get('/api/debts'),
  getSummary: () => api.get('/api/debts/summary'),
  getOne: (id) => api.get(`/api/debts/${id}`),
  create: (data) => api.post('/api/debts', data),
  update: (id, data) => api.put(`/api/debts/${id}`, data),
  delete: (id) => api.delete(`/api/debts/${id}`),
  getProjection: (id) => api.get(`/api/debts/${id}/projection`),
  addPayment: (id, data) => api.post(`/api/debts/${id}/payments`, data),
  getPayments: (id) => api.get(`/api/debts/${id}/payments`),
  deletePayment: (id, paymentId) => api.delete(`/api/debts/${id}/payments/${paymentId}`),
  simulate: (data) => api.post('/api/debts/simulate', data),
  compareStrategies: (data) => api.post('/api/debts/strategies', data),
}