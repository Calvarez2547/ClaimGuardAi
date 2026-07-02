import { BarChart3, Settings } from 'lucide-react';

export function PlaceholderPage({ kind }: { kind: 'reports' | 'settings' }) {
  const Icon = kind === 'reports' ? BarChart3 : Settings;
  return (
    <section className="grid place-items-center min-h-[420px] text-center p-8 border border-app-border rounded bg-app-panel shadow-card">
      <Icon size={34} className="text-app-muted" />
      <h2 className="m-0 mt-3 text-app-text">{kind === 'reports' ? 'Reports placeholder' : 'Settings placeholder'}</h2>
      <p className="max-w-[560px] text-app-muted">
        This MVP keeps the implemented workflow focused on authenticated claims, notes, AI analysis,
        and dashboard summaries. This area is reserved for future backend-supported features.
      </p>
    </section>
  );
}
