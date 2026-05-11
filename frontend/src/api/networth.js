import api from './axios'

export const networthApi = {
  calculate: (data) => api.post('/api/networth', data),
  getHistory: () => api.get('/api/networth/history'),
  takeSnapshot: (type = 'TOTAL') => api.post(`/api/networth/snapshot?type=${type}`),
  getAssets: () => api.get('/api/networth/assets'),
  createAsset: (data) => api.post('/api/networth/assets', data),
  updateAsset: (id, data) => api.put(`/api/networth/assets/${id}`, data),
  deleteAsset: (id) => api.delete(`/api/networth/assets/${id}`),
}