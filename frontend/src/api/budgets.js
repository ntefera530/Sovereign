import api from './axios'

export const budgetsApi = {
  getAll: () => api.get('/api/budgets'),
  getOne: (id) => api.get(`/api/budgets/${id}`),
  create: (data) => api.post('/api/budgets', data),
  update: (id, data) => api.put(`/api/budgets/${id}`, data),
  delete: (id) => api.delete(`/api/budgets/${id}`),
  getSummary: (id) => api.get(`/api/budgets/${id}/summary`),
  createCategory: (id, data) => api.post(`/api/budgets/${id}/categories`, data),
  getCategories: (id) => api.get(`/api/budgets/${id}/categories`),
  updateCategory: (id, catId, data) => api.put(`/api/budgets/${id}/categories/${catId}`, data),
  deleteCategory: (id, catId) => api.delete(`/api/budgets/${id}/categories/${catId}`),
}