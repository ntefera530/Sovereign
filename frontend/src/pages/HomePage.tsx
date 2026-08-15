import { useEffect, useState, useCallback } from "react";
import { Plus, RefreshCw, Trash2, Wallet } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select } from "@/components/ui/select";
import { Modal } from "@/components/ui/modal";
import PlaidLinkButton from "@/components/plaid/PlaidLinkButton";
import { accountsApi } from "@/api/accounts";
import { netWorthApi } from "@/api/networth";
import { plaidApi } from "@/api/plaid";
import { formatCurrency, formatDate, enumLabel } from "@/lib/format";
import type { AccountResponse, AccountType, NetWorthResponse, PlaidItemResponse } from "@/types/types";

const ACCOUNT_TYPE_OPTIONS: { value: AccountType; label: string }[] = [
  "CHECKING",
  "SAVINGS",
  "CREDIT_CARD",
  "INVESTMENT",
  "LOAN",
  "CASH",
  "OTHER",
].map((v) => ({ value: v as AccountType, label: enumLabel(v) }));

export default function HomePage() {
  const [accounts, setAccounts] = useState<AccountResponse[]>([]);
  const [totalBalance, setTotalBalance] = useState(0);
  const [netWorth, setNetWorth] = useState<NetWorthResponse | null>(null);
  const [plaidItems, setPlaidItems] = useState<PlaidItemResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [addOpen, setAddOpen] = useState(false);

  const loadAll = useCallback(async () => {
    setError(null);
    try {
      const [accountSummary, nw, items] = await Promise.all([
        accountsApi.getAll(),
        netWorthApi.calculate("TOTAL"),
        plaidApi.getItems(),
      ]);
      setAccounts(accountSummary.accounts);
      setTotalBalance(accountSummary.totalBalance);
      setNetWorth(nw);
      setPlaidItems(items);
    } catch {
      setError("Couldn't load your data. Is the backend running?");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  const handleSync = async (itemId: string) => {
    await plaidApi.sync(itemId);
    loadAll();
  };

  const handleRemoveItem = async (itemId: string) => {
    await plaidApi.removeItem(itemId);
    loadAll();
  };

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold">Home</h1>
          <p className="text-sm text-muted-foreground">Your net worth and accounts at a glance.</p>
        </div>
        <div className="flex gap-2">
          <PlaidLinkButton onSuccess={loadAll} />
          <Button onClick={() => setAddOpen(true)}>
            <Plus className="size-4" />
            Add account
          </Button>
        </div>
      </div>

      {error && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive">
          {error}
        </div>
      )}

      <div className="grid gap-4 sm:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>Net worth</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-semibold">
              {loading ? "—" : formatCurrency(netWorth?.netWorth ?? 0)}
            </div>
            {netWorth && (
              <p className="mt-1 text-xs text-muted-foreground">
                as of {formatDate(netWorth.calculatedAt)}
              </p>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Total assets</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-semibold text-emerald-600 dark:text-emerald-400">
              {loading ? "—" : formatCurrency(netWorth?.totalAssets ?? 0)}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Total liabilities</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-semibold text-destructive">
              {loading ? "—" : formatCurrency(netWorth?.totalLiabilities ?? 0)}
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader className="flex-row items-center justify-between">
          <CardTitle className="text-foreground text-base font-semibold">
            Accounts ({accounts.length})
          </CardTitle>
          <span className="text-sm text-muted-foreground">
            {formatCurrency(totalBalance)} total
          </span>
        </CardHeader>
        <CardContent className="flex flex-col gap-2">
          {loading && <p className="text-sm text-muted-foreground">Loading accounts…</p>}
          {!loading && accounts.length === 0 && (
            <p className="text-sm text-muted-foreground">
              No accounts yet. Connect a bank or add one manually.
            </p>
          )}
          {accounts.map((acc) => (
            <div
              key={acc.id}
              className="flex items-center justify-between rounded-lg border border-border px-3 py-2.5"
            >
              <div className="flex items-center gap-3">
                <div className="flex size-8 items-center justify-center rounded-full bg-muted">
                  <Wallet className="size-4 text-muted-foreground" />
                </div>
                <div>
                  <p className="text-sm font-medium">{acc.name}</p>
                  <p className="text-xs text-muted-foreground">{enumLabel(acc.type)}</p>
                </div>
              </div>
              <div className="flex items-center gap-2">
                {!acc.isActive && <Badge variant="secondary">Inactive</Badge>}
                <span className="text-sm font-medium tabular-nums">
                  {formatCurrency(acc.balance, acc.currency)}
                </span>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {plaidItems.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-foreground text-base font-semibold">
              Connected institutions
            </CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-2">
            {plaidItems.map((item) => (
              <div
                key={item.id}
                className="flex items-center justify-between rounded-lg border border-border px-3 py-2.5"
              >
                <div>
                  <p className="text-sm font-medium">{item.institutionName}</p>
                  <p className="text-xs text-muted-foreground">
                    {item.accountCount} account{item.accountCount === 1 ? "" : "s"} · last synced{" "}
                    {formatDate(item.lastSyncedAt)}
                  </p>
                </div>
                <div className="flex items-center gap-1.5">
                  <Badge
                    variant={
                      item.syncStatus === "SUCCESS"
                        ? "success"
                        : item.syncStatus === "FAILED"
                          ? "destructive"
                          : "secondary"
                    }
                  >
                    {enumLabel(item.syncStatus)}
                  </Badge>
                  <Button variant="ghost" size="icon-sm" onClick={() => handleSync(item.id)}>
                    <RefreshCw className="size-3.5" />
                  </Button>
                  <Button variant="ghost" size="icon-sm" onClick={() => handleRemoveItem(item.id)}>
                    <Trash2 className="size-3.5" />
                  </Button>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      <AddAccountModal
        open={addOpen}
        onOpenChange={setAddOpen}
        onCreated={loadAll}
      />
    </div>
  );
}

function AddAccountModal({
  open,
  onOpenChange,
  onCreated,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  onCreated: () => void;
}) {
  const [name, setName] = useState("");
  const [type, setType] = useState<AccountType>("CHECKING");
  const [initialBalance, setInitialBalance] = useState("0");
  const [currency, setCurrency] = useState("USD");
  const [saving, setSaving] = useState(false);

  const reset = () => {
    setName("");
    setType("CHECKING");
    setInitialBalance("0");
    setCurrency("USD");
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    try {
      await accountsApi.create({
        name,
        type,
        initialBalance: Number(initialBalance),
        currency,
      });
      reset();
      onOpenChange(false);
      onCreated();
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal open={open} onOpenChange={onOpenChange} title="Add account" description="Track a manual account, or use Plaid to connect one automatically.">
      <form onSubmit={handleSubmit} className="flex flex-col gap-3">
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="acc-name">Name</Label>
          <Input id="acc-name" value={name} onChange={(e) => setName(e.target.value)} placeholder="Chase Checking" required />
        </div>
        <div className="flex flex-col gap-1.5">
          <Label htmlFor="acc-type">Type</Label>
          <Select id="acc-type" value={type} onChange={(e) => setType(e.target.value as AccountType)} options={ACCOUNT_TYPE_OPTIONS} />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="acc-balance">Initial balance</Label>
            <Input id="acc-balance" type="number" step="0.01" value={initialBalance} onChange={(e) => setInitialBalance(e.target.value)} required />
          </div>
          <div className="flex flex-col gap-1.5">
            <Label htmlFor="acc-currency">Currency</Label>
            <Input id="acc-currency" value={currency} onChange={(e) => setCurrency(e.target.value.toUpperCase())} maxLength={3} required />
          </div>
        </div>
        <Button type="submit" disabled={saving} className="mt-2">
          {saving ? "Adding…" : "Add account"}
        </Button>
      </form>
    </Modal>
  );
}
