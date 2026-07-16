import React, { useState, useRef, useCallback } from "react";
import { uploadDocument, processDocument, submitCorrections } from "./api";
import {
  UploadCloud,
  FileText,
  Image as ImageIcon,
  Sparkles,
  CheckCircle2,
  AlertTriangle,
  AlertCircle,
  Pencil,
  ArrowRight,
  ArrowLeft,
  Check,
  RotateCcw,
  ScanLine,
} from "lucide-react";

/* ---------------------------------------------------------------
   Design tokens
   Palette: near-white surface, cool slate ink, one saturated
   "scanner" blue as the accent — everything else stays quiet so
   the confidence colors (the actual signal in this product) read
   clearly against it.
--------------------------------------------------------------- */
const C = {
  bg: "#F5F6F9",
  surface: "#FFFFFF",
  border: "#E4E7ED",
  borderStrong: "#CBD1DC",
  ink: "#161A23",
  inkMuted: "#6B7280",
  inkFaint: "#9CA3AF",
  accent: "#3452E1",
  accentSoft: "#EEF1FE",
  accentSoftBorder: "#D7DDFB",
  high: "#157F3C",
  highBg: "#EAF7EE",
  highBorder: "#BFE6CC",
  medium: "#B45309",
  mediumBg: "#FFF6E9",
  mediumBorder: "#F3DBAF",
  low: "#B42318",
  lowBg: "#FDF0EF",
  lowBorder: "#F3C6C1",
};

const FONT_UI =
  '-apple-system, BlinkMacSystemFont, "Segoe UI", Inter, system-ui, sans-serif';
const FONT_DATA =
  'ui-monospace, "SF Mono", "IBM Plex Mono", "Roboto Mono", monospace';

const STEPS = [
  { key: "upload", label: "Upload" },
  { key: "dashboard", label: "Review" },
  { key: "correction", label: "Correct" },
];

const INITIAL_FIELDS = [
  {
    id: "f1",
    label: "Invoice Number",
    value: "INV-2024-0157",
    confidence: 0.94,
    level: "HIGH",
  },
  {
    id: "f2",
    label: "Date",
    value: "12/03/2024",
    confidence: 0.89,
    level: "HIGH",
  },
  {
    id: "f3",
    label: "Vendor Name",
    value: "Nexora Supplies Pvt Ltd",
    confidence: 0.71,
    level: "MEDIUM",
  },
  {
    id: "f4",
    label: "Total Amount",
    value: "₹ 42,850.00",
    confidence: 0.48,
    level: "LOW",
  },
];

const SUMMARY_TEXT =
  "This is a commercial invoice from Nexora Supplies Pvt Ltd dated 12/03/2024, billed under invoice number INV-2024-0157. The document lists office equipment line items totaling ₹42,850.00, due within 30 days. Vendor identity and total amount were extracted with lower confidence and should be reviewed.";

function levelStyles(level) {
  if (level === "HIGH")
    return { fg: C.high, bg: C.highBg, border: C.highBorder, Icon: CheckCircle2, label: "High" };
  if (level === "MEDIUM")
    return { fg: C.medium, bg: C.mediumBg, border: C.mediumBorder, Icon: AlertTriangle, label: "Medium" };
  return { fg: C.low, bg: C.lowBg, border: C.lowBorder, Icon: AlertCircle, label: "Low" };
}

function ConfidenceBadge({ level }) {
  const s = levelStyles(level);
  return (
    <span
      className="inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-medium shrink-0"
      style={{ color: s.fg, backgroundColor: s.bg, border: `1px solid ${s.border}`, fontFamily: FONT_UI }}
    >
      <s.Icon size={12} strokeWidth={2.5} />
      {s.label}
    </span>
  );
}

function StepIndicator({ screen, unlocked, onNavigate }) {
  return (
    <ol className="flex items-center gap-2 sm:gap-3">
      {STEPS.map((step, i) => {
        const isActive = screen === step.key;
        const isUnlocked = unlocked.includes(step.key);
        return (
          <li key={step.key} className="flex items-center gap-2 sm:gap-3">
            <button
              type="button"
              disabled={!isUnlocked}
              onClick={() => isUnlocked && onNavigate(step.key)}
              className="flex items-center gap-2 rounded-full py-1 pl-1 pr-3 sm:pr-3.5 transition-colors focus-visible:outline focus-visible:outline-2"
              style={{
                fontFamily: FONT_UI,
                outlineColor: C.accent,
                backgroundColor: isActive ? C.accentSoft : "transparent",
                cursor: isUnlocked ? "pointer" : "default",
              }}
            >
              <span
                className="flex h-6 w-6 items-center justify-center rounded-full text-xs font-semibold"
                style={{
                  backgroundColor: isActive || isUnlocked ? C.accent : C.border,
                  color: isActive || isUnlocked ? "#fff" : C.inkFaint,
                }}
              >
                {isUnlocked && !isActive ? <Check size={12} strokeWidth={3} /> : i + 1}
              </span>
              <span
                className="hidden text-sm sm:inline"
                style={{ color: isActive ? C.accent : isUnlocked ? C.ink : C.inkFaint, fontWeight: isActive ? 600 : 500 }}
              >
                {step.label}
              </span>
            </button>
            {i < STEPS.length - 1 && (
              <span className="h-px w-4 sm:w-8" style={{ backgroundColor: C.border }} />
            )}
          </li>
        );
      })}
    </ol>
  );
}

function Header({ screen, unlocked, onNavigate }) {
  return (
    <header
      className="sticky top-0 z-10 flex flex-wrap items-center justify-between gap-3 border-b px-4 py-3 sm:px-6"
      style={{ backgroundColor: "rgba(255,255,255,0.9)", backdropFilter: "blur(6px)", borderColor: C.border }}
    >
      <div className="flex items-center gap-2">
        <div
          className="flex h-8 w-8 items-center justify-center rounded-lg"
          style={{ backgroundColor: C.accent }}
        >
          <ScanLine size={16} color="#fff" strokeWidth={2.5} />
        </div>
        <div>
          <p className="text-sm font-semibold leading-tight" style={{ color: C.ink, fontFamily: FONT_UI }}>
            DocIntel
          </p>
          <p className="hidden text-[11px] leading-tight sm:block" style={{ color: C.inkMuted, fontFamily: FONT_UI }}>
            AI Document Intelligence
          </p>
        </div>
      </div>
      <StepIndicator screen={screen} unlocked={unlocked} onNavigate={onNavigate} />
    </header>
  );
}

/* ---------------------------- Screen 1 ---------------------------- */

function UploadScreen({ onFileChosen, dragActive, setDragActive, processing, fileMeta }) {
  const inputRef = useRef(null);

  const handleDrop = useCallback(
    (e) => {
      e.preventDefault();
      setDragActive(false);
      const file = e.dataTransfer.files?.[0];
      if (file) onFileChosen(file);
    },
    [onFileChosen, setDragActive]
  );

  return (
    <div className="mx-auto flex max-w-2xl flex-col items-center px-4 py-12 sm:py-20">
      <h1
        className="text-center text-2xl font-semibold tracking-tight sm:text-3xl"
        style={{ color: C.ink, fontFamily: FONT_UI }}
      >
        Upload a document to get started
      </h1>
      <p className="mt-2 max-w-md text-center text-sm" style={{ color: C.inkMuted, fontFamily: FONT_UI }}>
        DocIntel reads your PDF or image, pulls out the key fields, and flags anything worth a second look.
      </p>

      <div
        onDragOver={(e) => {
          e.preventDefault();
          setDragActive(true);
        }}
        onDragLeave={() => setDragActive(false)}
        onDrop={handleDrop}
        onClick={() => inputRef.current?.click()}
        role="button"
        tabIndex={0}
        onKeyDown={(e) => e.key === "Enter" && inputRef.current?.click()}
        className="relative mt-8 flex w-full cursor-pointer flex-col items-center justify-center overflow-hidden rounded-2xl border-2 border-dashed px-6 py-14 text-center transition-colors focus-visible:outline focus-visible:outline-2"
        style={{
          borderColor: dragActive ? C.accent : C.borderStrong,
          backgroundColor: dragActive ? C.accentSoft : C.surface,
          outlineColor: C.accent,
        }}
      >
        {processing && (
          <div
            className="pointer-events-none absolute inset-x-0 h-24 opacity-70 [animation:scan_1.6s_ease-in-out_infinite]"
            style={{ background: `linear-gradient(180deg, transparent, ${C.accentSoft}, transparent)` }}
          />
        )}
        <div
          className="flex h-14 w-14 items-center justify-center rounded-full"
          style={{ backgroundColor: dragActive ? C.accent : C.accentSoft }}
        >
          <UploadCloud size={24} color={dragActive ? "#fff" : C.accent} strokeWidth={2} />
        </div>

        {!processing && !fileMeta && (
          <>
            <p className="mt-4 text-sm font-medium" style={{ color: C.ink, fontFamily: FONT_UI }}>
              Drag and drop your file here
            </p>
            <p className="mt-1 text-xs" style={{ color: C.inkMuted, fontFamily: FONT_UI }}>
              or click to browse — PDF, JPG, or PNG, up to 10MB
            </p>
          </>
        )}

        {fileMeta && (
          <div className="mt-4 flex items-center gap-2 rounded-lg border px-3 py-2" style={{ borderColor: C.border, backgroundColor: C.bg }}>
            {fileMeta.type === "pdf" ? <FileText size={16} color={C.accent} /> : <ImageIcon size={16} color={C.accent} />}
            <span className="text-xs font-medium" style={{ color: C.ink, fontFamily: FONT_DATA }}>
              {fileMeta.name}
            </span>
          </div>
        )}

        {processing && (
          <p className="mt-3 text-xs font-medium" style={{ color: C.accent, fontFamily: FONT_UI }}>
            Analyzing document…
          </p>
        )}

        <input
          ref={inputRef}
          type="file"
          accept=".pdf,.jpg,.jpeg,.png"
          className="hidden"
          onChange={(e) => e.target.files?.[0] && onFileChosen(e.target.files[0])}
        />
      </div>

      <button
        type="button"
        disabled={!fileMeta || processing}
        onClick={() => onFileChosen(null, true)}
        className="mt-6 flex w-full items-center justify-center gap-2 rounded-xl px-5 py-3 text-sm font-semibold transition-opacity focus-visible:outline focus-visible:outline-2 sm:w-auto sm:px-8"
        style={{
          backgroundColor: C.accent,
          color: "#fff",
          fontFamily: FONT_UI,
          opacity: !fileMeta || processing ? 0.5 : 1,
          cursor: !fileMeta || processing ? "not-allowed" : "pointer",
          outlineColor: C.accent,
        }}
      >
        {processing ? "Processing…" : "Process Document"}
        {!processing && <ArrowRight size={16} />}
      </button>

      <style>{`
        @keyframes scan { 0% { top: -6rem; } 100% { top: 100%; } }
        @media (prefers-reduced-motion: reduce) {
          * { animation: none !important; }
        }
      `}</style>
    </div>
  );
}

/* ---------------------------- Screen 2 ---------------------------- */

function DocumentPreview({ fileMeta }) {
  return (
    <div className="rounded-2xl border p-4 sm:p-5" style={{ backgroundColor: C.surface, borderColor: C.border }}>
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold" style={{ color: C.ink, fontFamily: FONT_UI }}>
          Document Preview
        </h2>
        <span
          className="inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-xs font-medium"
          style={{ backgroundColor: C.accentSoft, color: C.accent, border: `1px solid ${C.accentSoftBorder}`, fontFamily: FONT_UI }}
        >
          <Sparkles size={12} /> Invoice
        </span>
      </div>

      <div
        className="mt-4 flex aspect-[3/4] w-full flex-col justify-between rounded-xl border p-4"
        style={{ backgroundColor: C.bg, borderColor: C.border }}
      >
        <div className="flex items-center gap-2">
          <FileText size={16} color={C.inkFaint} />
          <span className="truncate text-xs" style={{ color: C.inkMuted, fontFamily: FONT_DATA }}>
            {fileMeta?.name ?? "document.pdf"}
          </span>
        </div>
        <div className="space-y-2">
          {[100, 85, 92, 60, 75].map((w, i) => (
            <div key={i} className="h-2 rounded-full" style={{ width: `${w}%`, backgroundColor: C.border }} />
          ))}
          <div className="h-2 w-1/3 rounded-full" style={{ backgroundColor: C.border }} />
        </div>
        <div className="flex items-end justify-between">
          <div className="space-y-1.5">
            <div className="h-2 w-16 rounded-full" style={{ backgroundColor: C.border }} />
            <div className="h-2 w-20 rounded-full" style={{ backgroundColor: C.border }} />
          </div>
          <div className="h-8 w-20 rounded-md" style={{ backgroundColor: C.accentSoft, border: `1px solid ${C.accentSoftBorder}` }} />
        </div>
      </div>

      <dl className="mt-4 grid grid-cols-2 gap-3 text-xs" style={{ fontFamily: FONT_UI }}>
        <div>
          <dt style={{ color: C.inkFaint }}>File type</dt>
          <dd className="font-medium" style={{ color: C.ink }}>{fileMeta?.type?.toUpperCase() ?? "PDF"}</dd>
        </div>
        <div>
          <dt style={{ color: C.inkFaint }}>Pages</dt>
          <dd className="font-medium" style={{ color: C.ink }}>1</dd>
        </div>
      </dl>
    </div>
  );
}

function FieldRow({ field }) {
  const s = levelStyles(field.level);
  return (
    <li
      className="flex items-center gap-3 rounded-lg border-l-4 border-y border-r py-3 pl-3 pr-3 sm:pl-4"
      style={{ borderLeftColor: s.fg, borderTopColor: C.border, borderRightColor: C.border, borderBottomColor: C.border, backgroundColor: C.surface }}
    >
      <div className="min-w-0 flex-1">
        <p className="text-xs" style={{ color: C.inkMuted, fontFamily: FONT_UI }}>{field.label}</p>
        <p className="mt-0.5 truncate text-sm font-medium" style={{ color: C.ink, fontFamily: FONT_DATA }}>
          {field.value || "—"}
        </p>
      </div>
      <ConfidenceBadge level={field.level} />
    </li>
  );
}

function DashboardScreen({ fields, fileMeta, summary, onGoToCorrections }) {
  const lowCount = fields.filter((f) => f.level !== "HIGH").length;

  return (
    <div className="mx-auto max-w-6xl px-4 py-6 sm:px-6 sm:py-8">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold tracking-tight sm:text-2xl" style={{ color: C.ink, fontFamily: FONT_UI }}>
            Extracted Data
          </h1>
          <p className="mt-1 text-sm" style={{ color: C.inkMuted, fontFamily: FONT_UI }}>
            Review the fields DocIntel pulled from your document.
          </p>
        </div>
        <button
          type="button"
          onClick={onGoToCorrections}
          className="flex items-center gap-2 rounded-xl border px-4 py-2.5 text-sm font-semibold transition-colors focus-visible:outline focus-visible:outline-2"
          style={{ borderColor: C.accent, color: C.accent, fontFamily: FONT_UI, outlineColor: C.accent }}
        >
          <Pencil size={14} /> Edit Fields
        </button>
      </div>

      {lowCount > 0 && (
        <div
          className="mt-5 flex items-center gap-2 rounded-xl border px-4 py-3 text-sm"
          style={{ backgroundColor: C.mediumBg, borderColor: C.mediumBorder, color: C.medium, fontFamily: FONT_UI }}
        >
          <AlertTriangle size={16} className="shrink-0" />
          {lowCount} field{lowCount > 1 ? "s" : ""} need review — confidence is below the reliable threshold.
        </div>
      )}

      <div className="mt-6 grid grid-cols-1 gap-5 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.15fr)]">
        <DocumentPreview fileMeta={fileMeta} />

        <div className="rounded-2xl border p-4 sm:p-5" style={{ backgroundColor: C.surface, borderColor: C.border }}>
          <h2 className="text-sm font-semibold" style={{ color: C.ink, fontFamily: FONT_UI }}>
            Structured Fields
          </h2>
          <ul className="mt-3 space-y-2.5">
            {fields.map((f) => (
              <FieldRow key={f.id} field={f} />
            ))}
          </ul>
        </div>
      </div>

      <div className="mt-5 rounded-2xl border p-4 sm:p-5" style={{ backgroundColor: C.surface, borderColor: C.border }}>
        <div className="flex items-center gap-2">
          <Sparkles size={16} color={C.accent} />
          <h2 className="text-sm font-semibold" style={{ color: C.ink, fontFamily: FONT_UI }}>
            Document Summary
          </h2>
        </div>
       <p className="mt-2.5 text-sm leading-relaxed" style={{ color: C.inkMuted, fontFamily: FONT_UI }}>
       {summary}
       </p>
      </div>
    </div>
  );
}

/* ---------------------------- Screen 3 ---------------------------- */

function CorrectionInput({ field, value, onChange }) {
  const flagged = field.level !== "HIGH";
  const s = levelStyles(field.level);
  const changed = value !== field.value;

  return (
    <div>
      <div className="flex items-center justify-between">
        <label className="text-xs font-medium" style={{ color: C.inkMuted, fontFamily: FONT_UI }}>
          {field.label}
        </label>
        {flagged && (
          <span className="flex items-center gap-1 text-[11px] font-medium" style={{ color: s.fg, fontFamily: FONT_UI }}>
            <s.Icon size={11} /> {s.label} confidence
          </span>
        )}
      </div>
      <input
        value={value}
        onChange={(e) => onChange(field.id, e.target.value)}
        className="mt-1.5 w-full rounded-lg border px-3 py-2.5 text-sm outline-none transition-colors focus:outline-2"
        style={{
          fontFamily: FONT_DATA,
          color: C.ink,
          backgroundColor: flagged ? s.bg : C.surface,
          borderColor: flagged ? s.border : C.border,
          outlineColor: C.accent,
        }}
      />
      {changed && (
        <p className="mt-1 text-[11px]" style={{ color: C.inkFaint, fontFamily: FONT_UI }}>
          Original: <span style={{ fontFamily: FONT_DATA }}>{field.value || "—"}</span>
        </p>
      )}
    </div>
  );
}

function CorrectionScreen({ fields, onBack, onSubmit }) {
  const [draft, setDraft] = useState(() =>
    Object.fromEntries(fields.map((f) => [f.id, f.value]))
  );
  const [submitted, setSubmitted] = useState(false);

  const handleChange = (id, val) => setDraft((d) => ({ ...d, [id]: val }));
  const resetField = (id, original) => setDraft((d) => ({ ...d, [id]: original }));

  const changedCount = fields.filter((f) => draft[f.id] !== f.value).length;

  const handleSubmit = () => {
    onSubmit(draft);
    setSubmitted(true);
  };

  return (
    <div className="mx-auto max-w-3xl px-4 py-6 sm:px-6 sm:py-8">
      <button
        type="button"
        onClick={onBack}
        className="flex items-center gap-1.5 text-sm font-medium focus-visible:outline focus-visible:outline-2"
        style={{ color: C.inkMuted, fontFamily: FONT_UI, outlineColor: C.accent }}
      >
        <ArrowLeft size={14} /> Back to review
      </button>

      <h1 className="mt-3 text-xl font-semibold tracking-tight sm:text-2xl" style={{ color: C.ink, fontFamily: FONT_UI }}>
        Correct Extracted Fields
      </h1>
      <p className="mt-1 text-sm" style={{ color: C.inkMuted, fontFamily: FONT_UI }}>
        Fields with medium or low confidence are highlighted. Edit any value that's wrong.
      </p>

      {submitted ? (
        <div
          className="mt-6 flex items-start gap-3 rounded-2xl border p-5"
          style={{ backgroundColor: C.highBg, borderColor: C.highBorder }}
        >
          <CheckCircle2 size={20} color={C.high} className="mt-0.5 shrink-0" />
          <div>
            <p className="text-sm font-semibold" style={{ color: C.high, fontFamily: FONT_UI }}>
              Corrections saved
            </p>
            <p className="mt-1 text-sm" style={{ color: C.ink, fontFamily: FONT_UI }}>
              {changedCount} field{changedCount === 1 ? "" : "s"} updated. These corrections feed DocIntel's
              feedback system, so repeat errors on this field type get flagged sooner next time.
            </p>
            <button
              type="button"
              onClick={onBack}
              className="mt-3 rounded-lg px-4 py-2 text-sm font-semibold"
              style={{ backgroundColor: C.accent, color: "#fff", fontFamily: FONT_UI }}
            >
              Back to dashboard
            </button>
          </div>
        </div>
      ) : (
        <>
          <div className="mt-6 space-y-5 rounded-2xl border p-4 sm:p-6" style={{ backgroundColor: C.surface, borderColor: C.border }}>
            {fields.map((f) => (
              <div key={f.id} className="flex items-start gap-2">
                <div className="flex-1">
                  <CorrectionInput field={f} value={draft[f.id]} onChange={handleChange} />
                </div>
                {draft[f.id] !== f.value && (
                  <button
                    type="button"
                    onClick={() => resetField(f.id, f.value)}
                    title="Revert to extracted value"
                    className="mt-6 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border focus-visible:outline focus-visible:outline-2"
                    style={{ borderColor: C.border, color: C.inkMuted, outlineColor: C.accent }}
                  >
                    <RotateCcw size={13} />
                  </button>
                )}
              </div>
            ))}
          </div>

          <div className="mt-5 flex flex-col-reverse gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-xs" style={{ color: C.inkFaint, fontFamily: FONT_UI }}>
              {changedCount === 0 ? "No changes yet" : `${changedCount} field${changedCount === 1 ? "" : "s"} changed`}
            </p>
            <button
              type="button"
              onClick={handleSubmit}
              className="flex items-center justify-center gap-2 rounded-xl px-6 py-3 text-sm font-semibold focus-visible:outline focus-visible:outline-2"
              style={{ backgroundColor: C.accent, color: "#fff", fontFamily: FONT_UI, outlineColor: C.accent }}
            >
              <Check size={16} /> Submit Corrections
            </button>
          </div>
        </>
      )}
    </div>
  );
}

/* ---------------------------- App ---------------------------- */

export default function App() {
  const [selectedFile, setSelectedFile] = useState(null);
  const [documentId, setDocumentId] = useState(null);
  const [summary, setSummary] = useState(SUMMARY_TEXT);
  const [screen, setScreen] = useState("upload");
  const [dragActive, setDragActive] = useState(false);
  const [processing, setProcessing] = useState(false);
  const [fileMeta, setFileMeta] = useState(null);
  const [fields, setFields] = useState(INITIAL_FIELDS);

  const unlocked = ["upload", ...(fileMeta ? ["dashboard"] : []), ...(fileMeta ? ["correction"] : [])];

  const handleFileChosen = async (file, startProcessing) => {
  if (file) {
    const ext = file.name.split(".").pop().toLowerCase();
    setFileMeta({ name: file.name, type: ext === "pdf" ? "pdf" : "image" });
    setSelectedFile(file);
    return;
  }
  if (startProcessing && selectedFile) {
    setProcessing(true);
    try {
      const uploadRes = await uploadDocument(selectedFile);
      setDocumentId(uploadRes.documentId);

      const processRes = await processDocument(uploadRes.documentId);
      setFields(processRes.fields);
      setSummary(processRes.summary);

      setProcessing(false);
      setScreen("dashboard");
    } catch (err) {
      setProcessing(false);
      console.error(err);
      alert("Something went wrong processing this document. Check the console.");
    }
  }
};

  const handleCorrectionsSubmit = async (draft) => {
  const corrections = fields
    .filter((f) => draft[f.id] !== f.value)
    .map((f) => ({ fieldId: f.id, correctedValue: draft[f.id] }));

  if (corrections.length === 0) return;

  try {
    const updated = await submitCorrections(documentId, corrections);
    setFields(updated.fields);
  } catch (err) {
    console.error(err);
    alert("Failed to save corrections. Check the console.");
  }
};
  return (
    <div className="min-h-screen w-full" style={{ backgroundColor: C.bg }}>
      <Header screen={screen} unlocked={unlocked} onNavigate={setScreen} />

      {screen === "upload" && (
        <UploadScreen
          onFileChosen={handleFileChosen}
          dragActive={dragActive}
          setDragActive={setDragActive}
          processing={processing}
          fileMeta={fileMeta}
        />
      )}

      {screen === "dashboard" && (
        <DashboardScreen
          fields={fields}
          summary={summary}
          fileMeta={fileMeta}
          onGoToCorrections={() => setScreen("correction")}
        />
      )}

      {screen === "correction" && (
        <CorrectionScreen
          fields={fields}
          onBack={() => setScreen("dashboard")}
          onSubmit={handleCorrectionsSubmit}
        />
      )}
    </div>
  );
}
