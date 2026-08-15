import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';

import { DebtResponse, AccountResponse } from '@/types/types';

// interface AccountsContextValue {
//   accounts: Account[] | null;
//   debts: Debt[] | null;
//   loading: boolean;
//   error: Error | null;
//   refreshData: () => Promise<void>;
// }

const AccountsContext = createContext<AccountsContextValue | undefined>(undefined);

interface AccountsProviderProps {
  children: ReactNode;
}

export function AccountsProvider({ children }: AccountsProviderProps) {

  // const [accounts, setAccounts] = useState<Account[] | null>(null);
  // const [debts, setDebts] = useState<Debt[] | null>(null);
  // const [loading, setLoading] = useState<boolean>(true);
  // const [error, setError] = useState<Error | null>(null);

  // const refreshData = useCallback(async () => {
  //   setLoading(true);
  //   setError(null);
  //   try {
  //     const [accountsRes, debtsRes] = await Promise.all([
  //       fetchAccounts(),
  //       fetchDebts(),
  //     ]);
  //     setAccounts(accountsRes);
  //     setDebts(debtsRes);
  //   } catch (e) {
  //     setError(e instanceof Error ? e : new Error('Failed to load account data'));
  //   } finally {
  //     setLoading(false);
  //   }
  // }, []);

  // useEffect(() => {
  //   refreshData();
  // }, [refreshData]);

  // const value: AccountsContextValue = {
  //   accounts,
  //   debts,
  //   loading,
  //   error,
  //   refreshData,
  // };

  // return (
  //   <AccountsContext.Provider value={value}>
  //     {children}
  //   </AccountsContext.Provider>
  // );
}

