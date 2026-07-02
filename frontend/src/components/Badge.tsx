import type { ReactNode } from 'react';

type BadgeProps = {
  children: ReactNode;
  tone?: 'success' | 'warning' | 'danger' | 'info' | 'neutral' | 'teal';
};

const toneClasses: Record<NonNullable<BadgeProps['tone']>, string> = {
  success: 'text-c-green bg-c-green-soft',
  warning: 'text-c-orange bg-c-orange-soft',
  danger:  'text-c-red bg-c-red-soft',
  info:    'text-primary bg-primary-soft',
  teal:    'text-c-teal bg-c-teal-soft',
  neutral: 'text-[#506175] bg-[#eef3f8]',
};

export function Badge({ children, tone = 'neutral' }: BadgeProps) {
  return (
    <span className={`inline-flex items-center w-fit min-h-[26px] px-[9px] py-1 rounded-[6px] text-xs font-extrabold whitespace-nowrap ${toneClasses[tone]}`}>
      {children}
    </span>
  );
}
