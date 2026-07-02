import { useState } from 'react';
import type { FormEvent } from 'react';
import { ShieldCheck } from 'lucide-react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ErrorState } from '../components/State';
import { env } from '../config/env';
import { friendlyError } from '../utils/errors';
import { getLastUsername } from '../auth/tokenStorage';

export function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState(() => getLastUsername() ?? 'local.analyst');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname || '/';

  if (isAuthenticated) {
    return <Navigate to={from} replace />;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');

    if (!username.trim() || !password.trim()) {
      setError('Username and password are required.');
      return;
    }

    setIsSubmitting(true);
    try {
      await login(username, password);
      navigate(from, { replace: true });
    } catch (loginError) {
      setError(friendlyError(loginError));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="grid grid-cols-[minmax(0,1.1fr)_430px] max-md2:grid-cols-[1fr] min-h-screen p-12 max-md2:p-[18px]"
      style={{
        background: 'linear-gradient(135deg, rgba(6,38,74,0.92), rgba(6,38,74,0.72)), radial-gradient(circle at 20% 20%, rgba(18,169,166,0.35), transparent 30%), #06264a',
      }}
    >
      {/* Left panel */}
      <section className="self-center max-w-[780px] text-white">
        <div className="flex items-center gap-3 text-[22px] font-extrabold">
          <div className="grid w-10 h-10 place-items-center border border-navy-accent/40 rounded bg-[rgba(25,119,210,0.16)] text-navy-accent">
            <ShieldCheck size={30} />
          </div>
          <span>ClaimGuard <strong className="text-navy-accent">AI</strong></span>
        </div>
        <h1 className="max-w-[680px] mt-[38px] mb-[18px] text-[58px] max-md2:text-[38px] leading-[1.02]">
          Revenue integrity claim review
        </h1>
        <p className="max-w-[650px] text-[#d4e5f8] text-[18px]">
          Sign in to review demo claims, add notes, run backend-owned AI-assisted analysis,
          and inspect dashboard reporting. Do not use real PHI.
        </p>
        <div className="grid grid-cols-3 max-md2:grid-cols-1 gap-[14px] max-w-[640px] mt-[34px]">
          {[
            { label: 'JWT', desc: 'Secure API access' },
            { label: 'AI', desc: 'Backend mediated' },
            { label: '0 PHI', desc: 'Demo data only' },
          ].map(({ label, desc }) => (
            <div key={label} className="p-[18px] border border-white/16 rounded bg-white/[0.08]">
              <strong className="block">{label}</strong>
              <span className="block text-[#b8d2ee]">{desc}</span>
            </div>
          ))}
        </div>
      </section>

      {/* Login card */}
      <section className="self-center grid gap-4 p-7 border border-app-border rounded bg-app-panel shadow-card">
        <h2 className="m-0 text-app-text text-2xl leading-tight">Log in</h2>
        {error ? <ErrorState message={error} /> : null}
        <form onSubmit={handleSubmit} className="grid gap-4">
          <label className="grid gap-[7px] text-[#33445f] font-extrabold">
            Username
            <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
          </label>
          <label className="grid gap-[7px] text-[#33445f] font-extrabold">
            Password
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
          </label>
          <button
            className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-primary text-white bg-primary"
            type="submit"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Signing in...' : 'Sign in'}
          </button>
        </form>
        <p className="text-app-muted text-xs">
          No account?{' '}
          <Link to="/register" className="text-primary font-bold">Create one</Link>
        </p>
        <div className="text-app-muted text-[13px]">
          API base URL: <code className="text-app-text">{env.apiBaseUrl}</code>
        </div>
      </section>
    </main>
  );
}
