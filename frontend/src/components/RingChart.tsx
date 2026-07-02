type RingChartProps = {
  segments: {
    label: string;
    value: number;
    color: string;
  }[];
  total: number;
  centerLabel?: string;
};

export function RingChart({ segments, total, centerLabel = 'Total' }: RingChartProps) {
  let start = 0;
  const gradient =
    total <= 0
      ? '#e5edf6 0 100%'
      : segments
          .filter((segment) => segment.value > 0)
          .map((segment) => {
            const percent = (segment.value / total) * 100;
            const end = start + percent;
            const stop = `${segment.color} ${start}% ${end}%`;
            start = end;
            return stop;
          })
          .join(', ');

  return (
    <div className="grid grid-cols-[190px_minmax(0,1fr)] items-center gap-6 pt-[22px]">
      <div
        className="grid w-[170px] h-[170px] place-items-center rounded-full"
        style={{ background: `conic-gradient(${gradient})` }}
      >
        <div className="grid w-24 h-24 place-items-center rounded-full bg-white">
          <strong className="text-[25px]">{total}</strong>
          <span className="text-app-muted text-xs">{centerLabel}</span>
        </div>
      </div>
      <div className="grid gap-[14px]">
        {segments.map((segment) => (
          <div key={segment.label} className="flex items-center gap-2.5">
            <span className="w-[9px] h-[9px] rounded-full flex-shrink-0" style={{ background: segment.color }} />
            <p className="flex-1 m-0 text-app-muted text-sm">{segment.label}</p>
            <strong>{segment.value}</strong>
          </div>
        ))}
      </div>
    </div>
  );
}
