import { isAxiosError } from "axios";

export function getErrorMessage(err: unknown, fallback = "Something went wrong. Try again.") {
  if (isAxiosError(err)) {
    const data = err.response?.data as { message?: string; error?: string } | undefined;
    return data?.message ?? data?.error ?? fallback;
  }
  if (err instanceof Error) return err.message;
  return fallback;
}
