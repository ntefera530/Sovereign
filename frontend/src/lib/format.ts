export function formatCurrency(value: number, currency = "USD") {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(value ?? 0);
}

export function formatPercent(value: number, fractionDigits = 1) {
  return `${(value ?? 0).toFixed(fractionDigits)}%`;
}

/** Backend stores interest rate as a decimal (0.2499 = 24.99%) */
export function formatRate(decimal: number) {
  return `${((decimal ?? 0) * 100).toFixed(2)}%`;
}

export function formatDate(iso: string | null | undefined) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

/** "CREDIT_CARD" -> "Credit Card" */
export function enumLabel(value: string) {
  return value
    .toLowerCase()
    .split("_")
    .map((w) => w[0].toUpperCase() + w.slice(1))
    .join(" ");
}
