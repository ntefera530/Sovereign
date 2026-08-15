import { useCallback, useEffect, useState } from "react";
import { usePlaidLink, type PlaidLinkOnSuccess } from "react-plaid-link";
import { Landmark, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { plaidApi } from "@/api/plaid";

interface PlaidLinkButtonProps {
  onSuccess?: () => void;
  label?: string;
}

export default function PlaidLinkButton({ onSuccess, label = "Connect a bank account" }: PlaidLinkButtonProps) {
  const [linkToken, setLinkToken] = useState<string | null>(null);
  const [loadingToken, setLoadingToken] = useState(false);
  const [exchanging, setExchanging] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setLoadingToken(true);
    plaidApi
      .createLinkToken()
      .then((res) => {
        if (!cancelled) setLinkToken(res.linkToken);
      })
      .catch(() => {
        if (!cancelled) setError("Couldn't start Plaid Link. Try again.");
      })
      .finally(() => {
        if (!cancelled) setLoadingToken(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const onPlaidSuccess = useCallback<PlaidLinkOnSuccess>(
    async (publicToken) => {
      if (!publicToken) return;
      setExchanging(true);
      setError(null);
      try {
        await plaidApi.exchangePublicToken(publicToken);
        onSuccess?.();
      } catch {
        setError("Connected, but linking the account failed. Try syncing again.");
      } finally {
        setExchanging(false);
      }
    },
    [onSuccess]
  );

  const { open, ready } = usePlaidLink({
    token: linkToken,
    onSuccess: onPlaidSuccess,
  });

  const busy = loadingToken || exchanging;

  return (
    <div className="flex flex-col items-start gap-1.5">
      <Button
        onClick={() => open()}
        disabled={!ready || busy}
        variant="outline"
      >
        {busy ? <Loader2 className="size-4 animate-spin" /> : <Landmark className="size-4" />}
        {label}
      </Button>
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  );
}
