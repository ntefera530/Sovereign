import api, { setTokens, clearTokens } from "./axios";

interface UserResponse {
  id: string;
  email: string;
}

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: UserResponse;
}

export const login = async (email: string, password: string) => {
  const { data } = await api.post<AuthResponse>("/auth/login", {
    email,
    password,
  });
  setTokens(data.accessToken, data.refreshToken);
  return data.user;
};

export const register = async ( email: string, password: string, firstName: string, lastName: string ) => {
  const { data } = await api.post<AuthResponse>("/auth/register", {
    email,
    password,
    firstName,
    lastName,
  });
  setTokens(data.accessToken, data.refreshToken);
  return data.user;
};

export const logout = async () => {
  await api.post("/auth/logout");
  clearTokens();
};