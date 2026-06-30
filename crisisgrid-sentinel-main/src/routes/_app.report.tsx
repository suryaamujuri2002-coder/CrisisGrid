import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { toast } from "sonner";
import { Loader2, MapPin } from "lucide-react";
import { api } from "@/lib/api";
import { CRISIS_SERVICE } from "@/lib/config";
import type { CrisisType } from "@/lib/types";

export const Route = createFileRoute("/_app/report")({
  head: () => ({ meta: [{ title: "Report Crisis — CrisisGrid" }] }),
  component: ReportPage,
});

const TYPES: CrisisType[] = [
  "FIRE",
  "FLOOD",
  "MEDICAL",
  "EARTHQUAKE",
  "ACCIDENT",
  "CHEMICAL",
  "INFRASTRUCTURE",
  "OTHER",
];

function ReportPage() {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [type, setType] = useState<CrisisType>("FIRE");
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [address, setAddress] = useState("");
  const [loading, setLoading] = useState(false);
  const [locating, setLocating] = useState(false);

  function useMyLocation() {
    if (!navigator.geolocation) {
      toast.error("Geolocation not supported");
      return;
    }
    setLocating(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setLatitude(pos.coords.latitude.toFixed(6));
        setLongitude(pos.coords.longitude.toFixed(6));
        setLocating(false);
      },
      (err) => {
        toast.error(err.message);
        setLocating(false);
      },
    );
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (title.length < 5 || title.length > 200)
      return toast.error("Title must be 5–200 chars");
    if (description.length < 10 || description.length > 2000)
      return toast.error("Description must be 10–2000 chars");

    setLoading(true);
    try {
      await api(`${CRISIS_SERVICE}/api/v1/crisis/report`, {
        method: "POST",
        body: {
          title,
          description,
          // FIX: backend CrisisReportRequest uses `crisisType`, not `type`.
          // Sending `type` would be silently ignored and the crisis would be
          // saved with crisisType = null, breaking the AI classifier.
          crisisType: type,
          latitude: parseFloat(latitude),
          longitude: parseFloat(longitude),
          address: address || undefined,
        },
      });
      toast.success("Crisis reported! AI analysis is running in the background.");
      navigate({ to: "/dashboard" });
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Failed to report");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-2xl mx-auto">
      <h1 className="text-2xl font-bold tracking-tight mb-1">Report a Crisis</h1>
      <p className="text-sm text-muted-foreground mb-6">
        Submit details — AI will classify and score severity automatically.
      </p>

      <form onSubmit={onSubmit} className="bg-card border rounded-lg p-6 space-y-5">
        <Field label="Title" hint={`${title.length}/200`}>
          <input
            required
            minLength={5}
            maxLength={200}
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
          />
        </Field>

        <Field label="Description" hint={`${description.length}/2000`}>
          <textarea
            required
            minLength={10}
            maxLength={2000}
            rows={5}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
          />
        </Field>

        <Field label="Crisis Type">
          <select
            value={type}
            onChange={(e) => setType(e.target.value as CrisisType)}
            className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
          >
            {TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </Field>

        <div className="grid grid-cols-2 gap-4">
          <Field label="Latitude">
            <input
              required
              type="number"
              step="any"
              value={latitude}
              onChange={(e) => setLatitude(e.target.value)}
              className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring tabular-nums"
            />
          </Field>
          <Field label="Longitude">
            <input
              required
              type="number"
              step="any"
              value={longitude}
              onChange={(e) => setLongitude(e.target.value)}
              className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring tabular-nums"
            />
          </Field>
        </div>

        <button
          type="button"
          onClick={useMyLocation}
          disabled={locating}
          className="inline-flex items-center gap-1.5 text-xs rounded-md border px-3 py-1.5 hover:bg-secondary"
        >
          {locating ? (
            <Loader2 className="h-3 w-3 animate-spin" />
          ) : (
            <MapPin className="h-3 w-3" />
          )}
          Use My Location
        </button>

        <Field label="Address (optional)">
          <input
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
          />
        </Field>

        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-md bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
        >
          {loading ? "Submitting…" : "Submit Crisis Report"}
        </button>
      </form>
    </div>
  );
}

function Field({
  label,
  hint,
  children,
}: {
  label: string;
  hint?: string;
  children: React.ReactNode;
}) {
  return (
    <div>
      <div className="flex items-center justify-between mb-1.5">
        <label className="text-sm font-medium">{label}</label>
        {hint && (
          <span className="text-[10px] text-muted-foreground tabular-nums">{hint}</span>
        )}
      </div>
      {children}
    </div>
  );
}