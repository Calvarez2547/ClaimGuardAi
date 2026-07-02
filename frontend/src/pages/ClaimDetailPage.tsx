import { Bot, CheckCircle2, FileText, Flag, RefreshCw, Save, Sparkles, TriangleAlert } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { claimGuardApi } from '../api/claimGuardApi';
import { Badge } from '../components/Badge';
import { EmptyState, ErrorState, LoadingState } from '../components/State';
import type { Claim, ClaimAnalysis, ClaimStatus, ReviewNote } from '../types/api';
import { formatCurrency, formatDate, formatDateTime, labelize, riskTone, statusTone } from '../utils/format';
import { friendlyError } from '../utils/errors';

const statusOptions: ClaimStatus[] = ['RECEIVED', 'DRAFT', 'SUBMITTED', 'IN_REVIEW', 'NEEDS_INFO', 'APPROVED', 'DENIED', 'CLOSED'];

const panelClass = 'p-5 border border-app-border rounded bg-app-panel shadow-card';
const miniCardClass = 'p-4 border border-app-border rounded';

export function ClaimDetailPage() {
  const params = useParams();
  const [searchParams, setSearchParams] = useSearchParams();
  const claimId = Number(params.claimId);
  const [claim, setClaim] = useState<Claim | null>(null);
  const [notes, setNotes] = useState<ReviewNote[]>([]);
  const [latestAnalysis, setLatestAnalysis] = useState<ClaimAnalysis | null>(null);
  const [history, setHistory] = useState<ClaimAnalysis[]>([]);
  const [noteText, setNoteText] = useState('');
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [isSavingStatus, setIsSavingStatus] = useState(false);
  const [isSavingNote, setIsSavingNote] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState<ClaimStatus>('RECEIVED');

  const loadClaimWorkspace = useCallback(async () => {
    setError(''); setNotice('');
    const [claimRes, notesRes, latestRes, historyRes] = await Promise.allSettled([
      claimGuardApi.getClaim(claimId),
      claimGuardApi.listReviewNotes(claimId),
      claimGuardApi.getLatestAnalysis(claimId),
      claimGuardApi.getAnalysisHistory(claimId),
    ]);
    if (claimRes.status === 'fulfilled') { setClaim(claimRes.value); setSelectedStatus(claimRes.value.claimStatus); }
    else throw claimRes.reason;
    if (notesRes.status === 'fulfilled') setNotes(notesRes.value);
    if (latestRes.status === 'fulfilled') setLatestAnalysis(latestRes.value); else setLatestAnalysis(null);
    if (historyRes.status === 'fulfilled') setHistory(historyRes.value); else setHistory([]);
  }, [claimId]);

  const runAnalysis = useCallback(async () => {
    setError(''); setNotice(''); setIsAnalyzing(true);
    try {
      const analysis = await claimGuardApi.analyzeClaim(claimId);
      setLatestAnalysis(analysis);
      setHistory(await claimGuardApi.getAnalysisHistory(claimId));
      setNotice('Analysis completed and persisted by the backend.');
    } catch (e) { setError(friendlyError(e)); }
    finally { setIsAnalyzing(false); }
  }, [claimId]);

  useEffect(() => {
    loadClaimWorkspace().catch((e) => setError(friendlyError(e))).finally(() => setIsLoading(false));
  }, [loadClaimWorkspace]);

  useEffect(() => {
    if (searchParams.get('analyze') === 'true' && !isLoading && claim) {
      void runAnalysis(); setSearchParams({});
    }
  }, [claim, isLoading, runAnalysis, searchParams, setSearchParams]);

  async function saveStatus() {
    setError(''); setNotice(''); setIsSavingStatus(true);
    try { const updated = await claimGuardApi.updateClaimStatus(claimId, selectedStatus); setClaim(updated); setNotice('Claim status updated.'); }
    catch (e) { setError(friendlyError(e)); }
    finally { setIsSavingStatus(false); }
  }

  async function addNote(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(''); setNotice('');
    if (!noteText.trim()) { setError('Review note text is required.'); return; }
    setIsSavingNote(true);
    try {
      const note = await claimGuardApi.addReviewNote(claimId, noteText.trim());
      setNotes((current) => [note, ...current]); setNoteText(''); setNotice('Review note added.');
    } catch (e) { setError(friendlyError(e)); }
    finally { setIsSavingNote(false); }
  }

  if (isLoading) return <LoadingState label="Loading claim workspace..." />;
  if (error && !claim) return <ErrorState message={error} />;
  if (!claim) return <EmptyState title="Claim not found" description="The backend did not return a claim for this ID." />;

  return (
    <div className="grid grid-cols-[minmax(0,1fr)_430px] max-lg2:grid-cols-[1fr] gap-[22px]">
      {/* Action row */}
      <div className="col-span-2 max-lg2:col-span-1 flex items-center justify-between gap-4">
        <Link to="/claims" className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-app-border text-app-text bg-white">Back to claims</Link>
        <button
          className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-primary text-white bg-primary"
          type="button" onClick={runAnalysis} disabled={isAnalyzing}
        >
          {isAnalyzing ? <RefreshCw size={17} className="spin" /> : <Sparkles size={17} />}
          {latestAnalysis ? 'Re-run Analysis' : 'Analyze Claim'}
        </button>
      </div>

      {error ? <div className="col-span-2 max-lg2:col-span-1"><ErrorState message={error} /></div> : null}
      {notice ? (
        <div className="col-span-2 max-lg2:col-span-1 grid gap-1 p-[12px_14px] rounded border border-[#bcebd5] text-[#0f7a52] bg-c-green-soft">
          <strong>{notice}</strong>
        </div>
      ) : null}

      {/* Claim summary strip */}
      <section className={`${panelClass} col-span-2 max-lg2:col-span-1 grid grid-cols-[1.2fr_repeat(4,minmax(0,1fr))] max-lg2:grid-cols-1 gap-[18px]`}>
        <div className="grid gap-[6px] pr-[14px] border-r border-app-border max-lg2:border-r-0 max-lg2:border-b max-lg2:pb-3 max-lg2:pr-0">
          <span className="text-app-muted text-xs font-extrabold uppercase">Claim ID</span>
          <strong className="text-[18px]">{claim.claimNumber}</strong>
          <Badge tone={statusTone(claim.claimStatus)}>{labelize(claim.claimStatus)}</Badge>
        </div>
        {[
          { label: 'Provider', main: claim.providerName, sub: claim.patientControlNumber || 'No patient control number' },
          { label: 'Payer', main: claim.payerName, sub: `Prior auth: ${claim.priorAuthRequired ? claim.priorAuthNumber || 'Required, missing' : 'Not required'}` },
          { label: 'Billed Amount', main: formatCurrency(claim.billedAmount), sub: `Service ${formatDate(claim.serviceDate)}` },
        ].map(({ label, main, sub }) => (
          <div key={label} className="grid gap-[6px] pr-[14px] border-r border-app-border max-lg2:border-r-0 max-lg2:border-b max-lg2:pb-3 max-lg2:pr-0 last:border-0 last:pb-0">
            <span className="text-app-muted text-xs font-extrabold uppercase">{label}</span>
            <strong className="text-[18px]">{main}</strong>
            <p className="m-0 text-app-muted text-[13px]">{sub}</p>
          </div>
        ))}
        <div className="grid gap-[6px]">
          <span className="text-app-muted text-xs font-extrabold uppercase">Workflow Status</span>
          <select value={selectedStatus} onChange={(e) => setSelectedStatus(e.target.value as ClaimStatus)}>
            {statusOptions.map((s) => <option key={s} value={s}>{labelize(s)}</option>)}
          </select>
          <button
            type="button" onClick={saveStatus} disabled={isSavingStatus}
            className="inline-flex items-center gap-[6px] w-fit border border-app-border rounded px-[10px] py-[7px] text-primary bg-white font-extrabold"
          >
            <Save size={15} /> Save
          </button>
        </div>
      </section>

      {/* Main analysis section */}
      <main className="grid gap-[22px] content-start">
        <section className={panelClass}>
          <div className="flex items-center justify-between gap-4">
            <h2 className="m-0 flex items-center gap-2 text-[18px]"><Bot size={19} /> AI Analysis Summary</h2>
            {latestAnalysis ? <span className="text-app-muted text-sm">Created {formatDateTime(latestAnalysis.createdAt)}</span> : null}
          </div>

          {latestAnalysis ? (
            <>
              {/* Hero metrics */}
              <div className="grid grid-cols-[1.4fr_repeat(3,minmax(0,1fr))] max-md2:grid-cols-1 gap-[18px] mt-[18px] pb-[18px] border-b border-app-border">
                <div className="grid gap-[9px]">
                  <span className="text-app-muted text-[13px] font-extrabold">Risk Score (0-100)</span>
                  <strong className="text-c-red text-[36px]">{latestAnalysis.riskScore}</strong>
                  <div className="w-full h-2 overflow-hidden rounded-full bg-gradient-to-r from-c-green via-[#e2c029] to-c-red">
                    <span className="block h-full border-r-[3px] border-app-text" style={{ width: `${latestAnalysis.riskScore}%` }} />
                  </div>
                </div>
                <div className="grid gap-[9px]">
                  <span className="text-app-muted text-[13px] font-extrabold">Risk Category</span>
                  <Badge tone={riskTone(latestAnalysis.riskCategory)}>{labelize(latestAnalysis.riskCategory)} Risk</Badge>
                </div>
                <div className="grid gap-[9px]">
                  <span className="text-app-muted text-[13px] font-extrabold">Human Review</span>
                  <Badge tone={latestAnalysis.humanReviewRequired ? 'danger' : 'success'}>{latestAnalysis.humanReviewRequired ? 'Recommended' : 'Optional'}</Badge>
                </div>
                <div className="grid gap-[9px]">
                  <span className="text-app-muted text-[13px] font-extrabold">Provider Fallback</span>
                  <Badge tone={latestAnalysis.fallbackUsed ? 'warning' : 'teal'}>{latestAnalysis.fallbackUsed ? 'Used' : 'Not used'}</Badge>
                </div>
              </div>

              {/* AI Narrative */}
              <article className="my-[18px] p-4 border border-[#b9d7fb] rounded bg-[#f0f7ff]">
                <h3 className="flex items-center gap-2 m-0 mb-2.5 text-base"><Sparkles size={17} /> AI-assisted narrative</h3>
                <p className="text-[#33445f] m-0">{latestAnalysis.aiSummary || 'No narrative summary was returned by the backend.'}</p>
              </article>

              {/* Analysis cards */}
              <div className="grid grid-cols-3 max-md2:grid-cols-1 gap-[14px]">
                <article className={miniCardClass}>
                  <h3 className="flex items-center gap-2 m-0 mb-2.5 text-base"><FileText size={17} /> Summary</h3>
                  <dl className="grid gap-2.5 m-0">
                    {[
                      ['Total score', latestAnalysis.scoreBreakdown?.totalScore ?? latestAnalysis.riskScore],
                      ['Capped score', latestAnalysis.scoreBreakdown?.cappedScore ?? latestAnalysis.riskScore],
                      ['Primary reason', latestAnalysis.primaryRiskReason],
                    ].map(([dt, dd]) => (
                      <div key={String(dt)} className="flex justify-between gap-3">
                        <dt className="text-app-muted">{dt}</dt>
                        <dd className="m-0 font-extrabold text-right">{dd}</dd>
                      </div>
                    ))}
                  </dl>
                </article>
                <article className={miniCardClass}>
                  <h3 className="flex items-center gap-2 m-0 mb-2.5 text-base"><TriangleAlert size={17} /> Risk Explanation</h3>
                  <p className="text-[#33445f] m-0">{latestAnalysis.primaryRiskReason}</p>
                  <ul className="mt-2 pl-[18px]">{latestAnalysis.secondaryRiskReasons.map((r) => <li key={r} className="text-[#33445f]">{r}</li>)}</ul>
                </article>
                <article className={miniCardClass}>
                  <h3 className="flex items-center gap-2 m-0 mb-2.5 text-base"><Flag size={17} /> Documentation Concerns</h3>
                  {latestAnalysis.findings.length > 0 ? (
                    <ul className="mt-0 pl-[18px]">{latestAnalysis.findings.map((f) => <li key={f.findingId} className="text-[#33445f]">{f.description}</li>)}</ul>
                  ) : (
                    <p className="text-[#33445f] m-0">No triggered documentation concerns in the latest analysis.</p>
                  )}
                </article>
                <article className={miniCardClass}>
                  <h3 className="flex items-center gap-2 m-0 mb-2.5 text-base"><CheckCircle2 size={17} /> Recommended Actions</h3>
                  {latestAnalysis.recommendedActions.length > 0 ? (
                    <ul className="mt-0 pl-[18px]">{latestAnalysis.recommendedActions.map((a) => <li key={a} className="text-[#33445f] [&::marker]:text-c-green">{a}</li>)}</ul>
                  ) : <p className="text-[#33445f] m-0">No recommended actions returned.</p>}
                </article>
                <article className={miniCardClass}>
                  <h3 className="m-0 mb-2.5 text-base">Review Priority</h3>
                  <p className="text-[#33445f] m-0">{latestAnalysis.humanReviewRequired ? 'Prioritize this claim for human review before operational decisions are finalized.' : 'Routine review is sufficient based on the latest backend analysis.'}</p>
                </article>
                <article className={miniCardClass}>
                  <h3 className="m-0 mb-2.5 text-base">Disclaimer</h3>
                  <p className="text-[#33445f] m-0">AI analysis is reviewer support only. It is not a final payer decision, clinical advice, legal advice, or a production PHI workflow.</p>
                </article>
              </div>
            </>
          ) : (
            <EmptyState
              title="No analysis yet"
              description="Run backend-owned analysis to generate a deterministic score, risk factors, and reviewer narrative."
              action={<button className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-primary text-white bg-primary" type="button" onClick={runAnalysis}>Analyze claim</button>}
            />
          )}
        </section>
      </main>

      {/* Sidebar */}
      <aside className="grid gap-[22px] content-start">
        {/* Review notes */}
        <section className={panelClass} id="notes">
          <div className="flex items-center justify-between gap-4 mb-3">
            <h2 className="m-0 text-[18px]">Review Notes</h2>
          </div>
          <form className="grid gap-4 mb-4" onSubmit={addNote}>
            <textarea value={noteText} onChange={(e) => setNoteText(e.target.value)} placeholder="Add a reviewer note..." rows={3} maxLength={2000} />
            <button
              className="inline-flex items-center justify-center gap-2 min-h-[40px] px-[14px] rounded font-extrabold border border-app-border text-primary bg-white"
              type="submit" disabled={isSavingNote}
            >
              {isSavingNote ? 'Adding...' : 'Add Note'}
            </button>
          </form>
          <div className="grid gap-[14px]">
            {notes.length > 0 ? notes.map((note) => (
              <article key={note.id} className="grid grid-cols-[38px_minmax(0,1fr)] gap-3 pt-[14px] border-t border-app-border">
                <div className="grid w-[38px] h-[38px] place-items-center rounded-full bg-[#17345c] text-white text-[13px] font-extrabold">RA</div>
                <div>
                  <strong className="block">Reviewer note</strong>
                  <span className="block text-app-muted text-[13px]">{formatDateTime(note.createdAt)}</span>
                  <p className="text-[#33445f] m-0 mt-1">{note.noteText}</p>
                </div>
              </article>
            )) : (
              <EmptyState title="No review notes" description="Add a note for this claim." />
            )}
          </div>
        </section>

        {/* Analysis history */}
        <section className={panelClass}>
          <div className="flex items-center justify-between gap-4 mb-3">
            <h2 className="m-0 text-[18px]">Analysis History</h2>
          </div>
          {history.length > 0 ? (
            <div className="grid gap-[14px]">
              {history.map((analysis, index) => (
                <article key={analysis.analysisId} className="relative grid grid-cols-[18px_minmax(0,1fr)] gap-3">
                  {index < history.length - 1 && (
                    <span className="absolute top-[18px] bottom-[-16px] left-[5px] w-[2px] bg-app-border" />
                  )}
                  <span className="w-3 h-3 mt-[5px] border-2 border-primary rounded-full bg-white z-10" />
                  <div>
                    <p className="block m-0 mb-1 text-app-muted text-[13px]">
                      {formatDateTime(analysis.createdAt)}{' '}
                      {index === 0 ? <Badge tone="info">Latest</Badge> : null}
                    </p>
                    <strong className="block m-0 mb-1">Risk Score: {analysis.riskScore} | {labelize(analysis.riskCategory)}</strong>
                    <span className="block text-app-muted text-[13px]">{analysis.humanReviewRequired ? 'Human review recommended' : 'Human review optional'}</span>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <EmptyState title="No history yet" description="Run analysis to create the first timeline entry." />
          )}
        </section>
      </aside>
    </div>
  );
}
