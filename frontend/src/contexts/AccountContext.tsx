import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';

import type { DebtResponse, AccountResponse, PlaidItemResponse } from '@/types/types';

import { accountsApi } from "@/api/accounts";
import { debtsApi } from "@/api/debts";
import { netWorthApi } from "@/api/networth";
import { plaidApi } from "@/api/plaid";

interface AccountsContextValue {
  accounts: AccountResponse[] | null;
  debts: DebtResponse[] | null;
  loading: boolean;
  error: Error | null;
  refreshData: () => Promise<void>;
}

const AccountsContext = createContext<AccountsContextValue | undefined>(undefined);

interface AccountsProviderProps {
  children: ReactNode;
}

export function AccountsProvider({ children }: AccountsProviderProps) {
  const [accounts, setAccounts] = useState<AccountResponse[] | null>(null);
  const [debts, setDebts] = useState<DebtResponse[] | null>(null);
  const [plaidItems, setPlaidItems] = useState<PlaidItemResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<Error | null>(null);

  const refreshData = useCallback(async () => {
    setLoading(true);
    setError(null);
    
    try {
      const [accountsRes, debtsRes, plaidItemsRes] = await Promise.all([
        accountsApi.getAll(),
        debtsApi.getAll(),
        plaidApi.getItems(),
      ]);

      setAccounts(accountsRes.accounts);
      setDebts(debtsRes);
      setPlaidItems(plaidItemsRes);

    } catch (e) {
      setError(e instanceof Error ? e : new Error('Failed to load account data'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refreshData();
  }, [refreshData]);

  //TODO: memoize the context value to prevent unnecessary re-renders
  const value: AccountsContextValue = {
    accounts,
    debts,
    loading,
    error,
    refreshData,
  };

  return (
    <AccountsContext.Provider value={value}>
      {children}
    </AccountsContext.Provider>
  );
}

export function useAccounts() : AccountsContextValue {
  const context = useContext(AccountsContext);
  if (context === undefined) {
    throw new Error("useAccounts must be used within an AccountsProvider");
  }
  return context;
}