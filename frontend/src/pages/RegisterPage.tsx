import { useState } from 'react';
import type { FormEvent } from 'react';
import { ShieldCheck } from 'lucide-react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { claimGuardApi } from '../api/claimGuardApi';
import { ErrorState } from '../components/State';
import { setStoredToken } from '../auth/tokenStorage';
import { friendlyError } from '../utils/errors';

export function RegisterPage() {
  const { isAuthenticated, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');

    if (!username.trim() || !email.trim() || !password) {
      setError('All fields are required.');
      return;
    }
    if (password !== confirm) {
      setError('Passwords do not match.');
      return;
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters.');
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await claimGuardApi.register(username.trim(), email.trim(), password);
      setStoredToken(response.accessToken, response.expiresAt, response.refreshToken);
      await refreshUser();
      navigate('/', { replace: true });
    } catch (e) {
      setError(friendlyError(e));
    } finally {
      setIsSubmitting(false);
    }
  }

  const labelClass = 'grid gap-[7px] text-[#33445f] font-extrabold';

  return (
    <main
      className="grid grid-cols-[minmax(0,1.1fr)_430px] max-md2:grid-cols-[1fr] min-h-screen p-12 max-md2:p-[18px]"
      style={{
        background:
          'linear-gradient(135deg, rgba(6,38,74,0.92), rgba(6,38,74,0.72)), radial-gradient(circle at 20% 20%, rgba(18,169,166,0.35), transparent 30%), #06264a',
      }}
    >
      {/* Left panel */}
      <section className="self-center max-w-[780px] text-white">
        <div className="flex items-center gap-3 text-[22px] font-extrabold">
          <div className="grid w-10 h-10 place-items-center border border-navy-accent/40 rounded bg-[rgba(25,119,210,0.16)] text-navy-accent">
            <ShieldCheck size={30} />
          </div>
          <span>
            ClaimGuard <strong className="text-navy-accent">AI</strong>
          </span>
        </div>
        <h1 className="max-w-[680px] mt-[38px] mb-[18px] text-[58px] max-md2:text-[38px] leading-[1.02]">
          Join the review workflow
        </h1>
        <p className="max-w-[650px] text-[#d4e5f8] text-[18px]">
          Create an account to access demo claim review, AI-assisted analysis, and dashboard
          reporting. No real PHI — demo data only.
        </p>
      </section>

      {/* Register card */}
      <section className="self-center grid gap-4 p-7 border border-app-border rounded bg-app-panel shadow-card">
        <h2 className="m-0 text-app-text text-2xl leading-tight">Create account</h2>
        <p className="m-0 text-app-muted text-sm">
          New accounts are created as Billing Specialist by default.
        </p>
        {error ? <ErrorState message={error} /> : null}
        <form onSubmit={handleSubmit} className="grid gap-4">
          <label className={labelClass}>
            Username
            <input value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" maxLength={50} />
          </label>
          <label className={labelClass}>
            Email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" maxLength={255} />
          </label>
          <label className={labelClass}>
            Password
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="new-password" />
          </label>
          <label className={labelClass}>
            Confirm password
            <input type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} autoComplete="new-password" />
          </label>
          <button
            className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-primary text-white bg-primary"
            type="submit"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Creating account...' : 'Create account'}
          </button>
        </form>
        <p className="text-app-muted text-sm">
          Already have an account?{' '}
          <Link to="/login" className="text-primary font-bold">
            Sign in
          </Link>
        </p>
      </section>
    </main>
  );
}
