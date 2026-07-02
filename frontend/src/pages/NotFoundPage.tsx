import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <main className="grid place-items-center min-h-screen p-8 bg-app-bg">
      <section className="grid place-items-center min-h-[420px] text-center p-8 border border-app-border rounded bg-app-panel shadow-card">
        <h1 className="m-0 text-app-text text-2xl">Page not found</h1>
        <p className="max-w-[560px] text-app-muted">The route you requested is not part of the ClaimGuard AI MVP.</p>
        <Link
          to="/"
          className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-primary text-white bg-primary"
        >
          Return to dashboard
        </Link>
      </section>
    </main>
  );
}
