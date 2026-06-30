import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Sparkles } from "lucide-react";
import { api } from "@/lib/api";
import { CRISIS_SERVICE } from "@/lib/config";
import type { Crisis, PageResponse } from "@/lib/types";
import { normaliseCrisis } from "@/lib/types";
import { SeverityPill, StatusBadge, TypeBadge } from "@/components/badges";
import { CrisisModal } from "@/components/crisis-modal";

export const Route = createFileRoute("/_app/my-reports")({
  head: () => ({ meta: [{ title: "My Reports — CrisisGrid" }] }),
  component: MyReportsPage,
});

function extractList<T>(
  data: unknown,
  normalise: (raw: Record<string, unknown>) => T,
): T[] {
  let raw: unknown[] = [];
  if (Array.isArray(data)) {
    raw = data;
  } else if (
    data &&
    typeof data === "object" &&
    Array.isArray((data as PageResponse<unknown>).content)
  ) {
    raw = (data as PageResponse<unknown>).content;
  }
  return raw.map((item) => normalise(item as Record<string, unknown>));
}

function MyReportsPage() {
  const [selected, setSelected] = useState<Crisis | null>(null);

  const query = useQuery({
    queryKey: ["crisis", "my-reports"],
    queryFn: () => api<unknown>(`${CRISIS_SERVICE}/api/v1/crisis/my-reports`),
    refetchInterval: 30000,
  });

  // FIX: normalise so crisisType → type and LocalDateTime arrays → ISO strings
  const reports = useMemo(
    () => extractList<Crisis>(query.data, normaliseCrisis),
    [query.data],
  );

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">My Reports</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Crises you've submitted, with live AI analysis updates.
        </p>
      </div>

      <div className="relative space-y-4 before:content-[''] before:absolute before:left-3 before:top-2 before:bottom-2 before:w-px before:bg-border">
        {query.isLoading && (
          <p className="text-sm text-muted-foreground pl-8">Loading…</p>
        )}
        {!query.isLoading && reports.length === 0 && (
          <p className="text-sm text-muted-foreground pl-8">
            You haven't reported any crises yet.
          </p>
        )}
        {reports.map((r) => {
          const score = r.severityScore ?? r.severity ?? null;
          return (
            <div key={r.id} className="relative pl-8">
              <div className="absolute left-1.5 top-4 h-3 w-3 rounded-full bg-primary border-2 border-background" />
              <button
                onClick={() => setSelected(r)}
                className="w-full text-left bg-card border rounded-lg p-4 hover:border-primary/50"
              >
                <div className="flex items-start justify-between gap-2 mb-2">
                  <div className="flex flex-wrap items-center gap-2">
                    <TypeBadge type={r.type} />
                    <StatusBadge status={r.status} />
                    <span className="text-[10px] uppercase tracking-wider text-muted-foreground">
                      {new Date(r.reportedAt ?? r.createdAt ?? Date.now()).toLocaleString()}
                    </span>
                  </div>
                  <SeverityPill value={score} />
                </div>
                <h3 className="font-semibold mb-1">{r.title}</h3>
                {r.aiSummary ? (
                  <div className="mt-2 flex gap-2 text-sm text-muted-foreground italic border-l-2 border-accent/50 pl-3">
                    <Sparkles className="h-3.5 w-3.5 mt-0.5 shrink-0 text-accent" />
                    {r.aiSummary}
                  </div>
                ) : (
                  <div className="mt-2 flex items-center gap-2 text-xs text-muted-foreground">
                    <span className="inline-block h-2 w-2 rounded-full bg-accent animate-pulse" />
                    AI analysis in progress…
                  </div>
                )}
              </button>
            </div>
          );
        })}
      </div>

      {selected && <CrisisModal crisis={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}