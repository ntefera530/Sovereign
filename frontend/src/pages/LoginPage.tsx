import React, { useState } from 'react'
import { Link,  useLocation, useNavigate } from 'react-router-dom';
import { Landmark, Loader2 } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

import { login } from "@/api/auth";



const LoginPage = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const from = location.state?.from?.pathname || "/";

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await login(email, password);
            navigate(from, { replace: true });
        } catch (error) {
            console.error('Login failed:', error);
            setError('Invalid email or password');
        } finally {
            setSubmitting(false);
        }
    }

  return (
    <div className="flex min-h-screen items-center justify-center bg-background px-4 text-foreground">
      <div className="w-full max-w-sm">
        <div className="mb-8 flex flex-col items-center gap-2">
          <Landmark className="size-7" />
          <h1 className="text-lg font-semibold">Sign in to Sovereign</h1 >
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-4 rounded-xl border border-border bg-card p-6 shadow-sm">
          {error && (
            <div className="rounded-lg border border-destructive/30 bg-destructive/5 px-3 py-2 text-sm text-destructive">
              {error}
            </div>
          )}

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="login-email">Email</Label>
            <Input
              id="login-email"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              required
            />
          </div>

          <div className="flex flex-col gap-1.5">
            <Label htmlFor="login-password">Password</Label>
            <Input
              id="login-password"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
          </div>

          <Button type="submit" disabled={submitting} className="mt-2 w-full justify-center">
            {submitting ? <Loader2 className="size-4 animate-spin" /> : null}
            {submitting ? "Signing in…" : "Sign in"}
          </Button>
        </form>

        <p className="mt-4 text-center text-sm text-muted-foreground">
          Don't have an account?{" "}
          <Link to="/signup" className="font-medium text-foreground underline-offset-4 hover:underline">
            Create one
          </Link>
        </p>
      </div>
    </div>
  );
}

export default LoginPage