import type { ReactNode } from 'react';

export function LoadingState({ label = 'Loading...' }: { label?: string }) {
  return (
    <div className="grid place-items-center gap-2.5 min-h-[180px] p-7 border border-dashed border-app-border rounded text-app-muted text-center bg-app-panel-soft">
      <div className="w-7 h-7 border-[3px] border-app-border border-t-primary rounded-full animate-spin" />
      <p className="m-0">{label}</p>
    </div>
  );
}

export function EmptyState({
  title,
  description,
  action,
}: {
  title: string;
  description: string;
  action?: ReactNode;
}) {
  return (
    <div className="grid place-items-center gap-2.5 min-h-[180px] p-7 border border-dashed border-app-border rounded text-app-muted text-center bg-app-panel-soft">
      <h3 className="m-0 text-app-text">{title}</h3>
      <p className="m-0">{description}</p>
      {action}
    </div>
  );
}

export function ErrorState({ message }: { message: string }) {
  return (
    <div className="grid gap-1 p-[12px_14px] rounded border border-[#ffc5c5] text-[#9f1d1d] bg-c-red-soft">
      <strong>We could not complete that request.</strong>
      <span>{message}</span>
    </div>
  );
}
