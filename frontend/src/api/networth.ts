import api from "./axios";

import type {
  NetWorthResponse,
  NetWorthHistoryResponse,
  NetWorthSnapshotResponse,
  ManualAssetResponse,
  CreateAssetRequest,
  UpdateAssetRequest,
  NetWorthCalculationType,
} from "@/types/types";

export const netWorthApi = {
  calculate: (calculationType: NetWorthCalculationType = "TOTAL", customAccountIds?: string[]) =>
    api
      .post<NetWorthResponse>("/networth", { calculationType, customAccountIds })
      .then((r) => r.data),

  takeSnapshot: (type: NetWorthCalculationType = "TOTAL") =>
    api
      .post<NetWorthSnapshotResponse>(`/networth/snapshot?type=${type}`)
      .then((r) => r.data),

  getHistory: () => api.get<NetWorthHistoryResponse>("/networth/history").then((r) => r.data),

  getAssets: () => api.get<ManualAssetResponse[]>("/networth/assets").then((r) => r.data),

  createAsset: (body: CreateAssetRequest) =>
    api.post<ManualAssetResponse>("/networth/assets", body).then((r) => r.data),

  updateAsset: (id: string, body: UpdateAssetRequest) =>
    api.put<ManualAssetResponse>(`/networth/assets/${id}`, body).then((r) => r.data),

  deleteAsset: (id: string) => api.delete(`/networth/assets/${id}`),
};
