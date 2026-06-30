import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import { Loader2, MapPin, Search } from "lucide-react";
import { toast } from "sonner";
import { api } from "@/lib/api";
import { CRISIS_SERVICE } from "@/lib/config";
import type { Crisis } from "@/lib/types";
import { normaliseCrisis } from "@/lib/types";
import { SeverityPill, StatusBadge, TypeBadge } from "@/components/badges";
import { CrisisModal } from "@/components/crisis-modal";
import { CrisisMap } from "@/components/crisis-map";

export const Route = createFileRoute("/_app/nearby")({
  head: () => ({ meta: [{ title: "Nearby — CrisisGrid" }] }),
  component: NearbyPage,
});

function NearbyPage() {
  const [lat, setLat] = useState("");
  const [lng, setLng] = useState("");
  const [radius, setRadius] = useState("10");
  const [results, setResults] = useState<Crisis[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [selected, setSelected] = useState<Crisis | null>(null);

  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition((pos) => {
        setLat(pos.coords.latitude.toFixed(6));
        setLng(pos.coords.longitude.toFixed(6));
      });
    }
  }, []);

  async function search(e?: React.FormEvent) {
    e?.preventDefault();
    setLoading(true);
    try {
      const data = await api<unknown>(
        `${CRISIS_SERVICE}/api/v1/crisis/nearby?lat=${lat}&lng=${lng}&radiusKm=${radius}`,
      );

      // FIX: normalise every item so crisisType → type and dates are strings
      let raw: unknown[] = [];
      if (Array.isArray(data)) {
        raw = data;
      } else if (data && typeof data === "object" && Array.isArray((data as { content?: unknown[] }).content)) {
        raw = (data as { content: unknown[] }).content;
      }
      setResults(raw.map((item) => normaliseCrisis(item as Record<string, unknown>)));
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Search failed");
      setResults([]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Nearby Crises</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Search active incidents by location and radius.
        </p>
      </div>

      <form
        onSubmit={search}
        className="bg-card border rounded-lg p-4 grid grid-cols-1 md:grid-cols-4 gap-3"
      >
        <input
          required
          placeholder="Latitude"
          type="number"
          step="any"
          value={lat}
          onChange={(e) => setLat(e.target.value)}
          className="rounded-md border bg-input/40 px-3 py-2 text-sm tabular-nums"
        />
        <input
          required
          placeholder="Longitude"
          type="number"
          step="any"
          value={lng}
          onChange={(e) => setLng(e.target.value)}
          className="rounded-md border bg-input/40 px-3 py-2 text-sm tabular-nums"
        />
        <input
          required
          placeholder="Radius (km)"
          type="number"
          min={1}
          value={radius}
          onChange={(e) => setRadius(e.target.value)}
          className="rounded-md border bg-input/40 px-3 py-2 text-sm tabular-nums"
        />
        <button
          type="submit"
          disabled={loading}
          className="inline-flex items-center justify-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
        >
          {loading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
          Search
        </button>
      </form>

      {results && results.length > 0 && (
        <CrisisMap
          center={[parseFloat(lat) || 0, parseFloat(lng) || 0]}
          radiusKm={parseFloat(radius) || 10}
          crises={results}
          onSelect={setSelected}
        />
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {results == null && (
          <p className="text-sm text-muted-foreground col-span-full">
            Run a search to see nearby crises.
          </p>
        )}
        {results && results.length === 0 && (
          <p className="text-sm text-muted-foreground col-span-full">
            No crises within {radius} km.
          </p>
        )}
        {results?.map((c) => (
          <button
            key={c.id}
            onClick={() => setSelected(c)}
            className="text-left bg-card border rounded-lg p-4 hover:border-primary/50 transition-colors"
          >
            <div className="flex items-start justify-between gap-2 mb-2">
              <div className="flex items-center gap-2">
                <TypeBadge type={c.type} />
                <StatusBadge status={c.status} />
              </div>
              <SeverityPill value={c.severityScore ?? c.severity} />
            </div>
            <h3 className="font-semibold mb-1">{c.title}</h3>
            <div className="flex items-center gap-1 text-xs text-muted-foreground">
              <MapPin className="h-3 w-3" />
              {c.distanceKm != null
                ? `${c.distanceKm.toFixed(1)} km away`
                : `${c.latitude.toFixed(3)}, ${c.longitude.toFixed(3)}`}
            </div>
          </button>
        ))}
      </div>

      {selected && <CrisisModal crisis={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}