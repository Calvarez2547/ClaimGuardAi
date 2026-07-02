import { useState } from 'react';
import type { FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { claimGuardApi } from '../api/claimGuardApi';
import { ErrorState } from '../components/State';
import type { CreateClaimPayload } from '../types/api';
import { friendlyError } from '../utils/errors';

const initialForm = {
  claimNumber: `CLM-DEMO-${Math.floor(Date.now() / 1000)}`,
  patientControlNumber: '',
  payerName: 'Acme Health Plan',
  providerName: 'North Valley Clinic',
  serviceDate: new Date().toISOString().slice(0, 10),
  billedAmount: '1250.75',
  priorAuthRequired: false,
  priorAuthNumber: '',
  claimNotes: 'Demo claim intake documentation with sufficient administrative detail. No real PHI.',
};

const labelClass = 'grid gap-[7px] text-[#33445f] font-extrabold';

export function CreateClaimPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(initialForm);
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  function updateField(field: keyof typeof form, value: string | boolean) {
    setForm((current) => ({ ...current, [field]: value }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');

    if (!form.claimNumber.trim() || !form.payerName.trim() || !form.providerName.trim() || !form.serviceDate || !form.billedAmount) {
      setError('Claim number, payer, provider, service date, and billed amount are required.');
      return;
    }

    const payload: CreateClaimPayload = {
      claimNumber: form.claimNumber.trim(),
      patientControlNumber: form.patientControlNumber.trim() || null,
      payerName: form.payerName.trim(),
      providerName: form.providerName.trim(),
      serviceDate: form.serviceDate,
      billedAmount: Number(form.billedAmount),
      priorAuthRequired: form.priorAuthRequired,
      priorAuthNumber: form.priorAuthNumber.trim() || null,
      claimNotes: form.claimNotes.trim() || null,
    };

    setIsSubmitting(true);
    try {
      const claim = await claimGuardApi.createClaim(payload);
      navigate(`/claims/${claim.id}`);
    } catch (e) {
      setError(friendlyError(e));
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="grid gap-[22px]">
      <section className="p-5 border border-app-border rounded bg-app-panel shadow-card">
        <h2 className="m-0 text-app-text text-2xl leading-tight">Create a demo claim</h2>
        <p className="mt-1 mb-4 text-app-muted text-sm">Use fake data only. The backend validates required fields and service dates.</p>
        {error ? <div className="mb-4"><ErrorState message={error} /></div> : null}
        <form
          className="grid grid-cols-2 max-md2:grid-cols-1 gap-4 mt-[18px]"
          onSubmit={handleSubmit}
        >
          <label className={labelClass}>
            Claim number
            <input value={form.claimNumber} onChange={(e) => updateField('claimNumber', e.target.value)} maxLength={80} />
          </label>
          <label className={labelClass}>
            Patient control number
            <input value={form.patientControlNumber} onChange={(e) => updateField('patientControlNumber', e.target.value)} maxLength={80} placeholder="PCN-DEMO-1001" />
          </label>
          <label className={labelClass}>
            Payer name
            <input value={form.payerName} onChange={(e) => updateField('payerName', e.target.value)} maxLength={255} />
          </label>
          <label className={labelClass}>
            Provider name
            <input value={form.providerName} onChange={(e) => updateField('providerName', e.target.value)} maxLength={255} />
          </label>
          <label className={labelClass}>
            Service date
            <input type="date" value={form.serviceDate} onChange={(e) => updateField('serviceDate', e.target.value)} max={new Date().toISOString().slice(0, 10)} />
          </label>
          <label className={labelClass}>
            Billed amount
            <input type="number" min="0.01" step="0.01" value={form.billedAmount} onChange={(e) => updateField('billedAmount', e.target.value)} />
          </label>
          <label className="flex col-span-2 max-md2:col-span-1 items-center gap-2.5 text-[#33445f] font-extrabold">
            <input type="checkbox" className="w-[18px] min-h-[18px]" checked={form.priorAuthRequired} onChange={(e) => updateField('priorAuthRequired', e.target.checked)} />
            Prior authorization required
          </label>
          <label className={labelClass}>
            Prior authorization number
            <input value={form.priorAuthNumber} onChange={(e) => updateField('priorAuthNumber', e.target.value)} maxLength={80} />
          </label>
          <label className={`${labelClass} col-span-2 max-md2:col-span-1`}>
            Claim notes
            <textarea value={form.claimNotes} onChange={(e) => updateField('claimNotes', e.target.value)} maxLength={2000} rows={5} />
          </label>
          <div className="col-span-2 max-md2:col-span-1 flex justify-end">
            <button
              className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-primary text-white bg-primary"
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Creating...' : 'Create claim'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
