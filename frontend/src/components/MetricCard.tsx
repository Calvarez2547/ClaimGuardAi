import type { LucideIcon } from 'lucide-react';

type MetricCardProps = {
  label: string;
  value: string | number;
  delta?: string;
  tone?: 'blue' | 'red' | 'orange' | 'teal';
  icon: LucideIcon;
};

const iconClasses: Record<NonNullable<MetricCardProps['tone']>, string> = {
  blue:   'text-primary bg-primary-soft',
  red:    'text-c-red bg-c-red-soft',
  orange: 'text-c-orange bg-c-orange-soft',
  teal:   'text-c-teal bg-c-teal-soft',
};

export function MetricCard({ label, value, delta, tone = 'blue', icon: Icon }: MetricCardProps) {
  return (
    <article className="flex items-center gap-[18px] min-h-[126px] p-5 border border-app-border rounded bg-app-panel shadow-card">
      <div className={`grid w-[58px] h-[58px] place-items-center rounded ${iconClasses[tone]}`}>
        <Icon size={25} />
      </div>
      <div>
        <p className="m-0 text-app-muted text-[13px]">{label}</p>
        <strong className="block mt-0.5 mb-1 text-[27px]">{value}</strong>
        {delta ? <span className="text-app-muted text-[13px]">{delta}</span> : null}
      </div>
    </article>
  );
}
