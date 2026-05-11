import api from './axios'

export const billsApi = {
  split: (data) => api.post('/api/bills/split', data),
}