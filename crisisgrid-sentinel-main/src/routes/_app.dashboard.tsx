import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { Activity, AlertTriangle, Sparkles, Truck } from "lucide-react";
import { api } from "@/lib/api";
import { CRISIS_SERVICE, RESOURCE_SERVICE } from "@/lib/config";
import type { Crisis, PageResponse, Resource } from "@/lib/types";
import { normaliseCrisis, normaliseResource } from "@/lib/types";
import { SeverityPill, StatusBadge, TypeBadge } from "@/components/badges";
import { CrisisModal } from "@/components/crisis-modal";

export const Route = createFileRoute("/_app/dashboard")({
  head: () => ({ meta: [{ title: "Dashboard — CrisisGrid" }] }),
  component: Dashboard,
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

function Dashboard() {
  const [selected, setSelected] = useState<Crisis | null>(null);

  const crisesQuery = useQuery({
    queryKey: ["crisis", "active"],
    queryFn: () => api<unknown>(`${CRISIS_SERVICE}/api/v1/crisis/active?page=0&size=20`),
    refetchInterval: 15000,
  });

  const resourcesQuery = useQuery({
    queryKey: ["resources", "available"],
    queryFn: () => api<unknown>(`${RESOURCE_SERVICE}/api/v1/resources/available`),
    refetchInterval: 30000,
  });

  // FIX: run normaliseCrisis on every item so crisisType → type and
  // LocalDateTime arrays → ISO strings are handled before rendering.
  const crises = useMemo(
    () => extractList<Crisis>(crisesQuery.data, normaliseCrisis),
    [crisesQuery.data],
  );

  const resources = useMemo(
    () => extractList<Resource>(resourcesQuery.data, normaliseResource),
    [resourcesQuery.data],
  );

  const stats = useMemo(() => {
    const critical = crises.filter((c) => (c.severityScore ?? c.severity ?? 0) >= 8).length;
    const pendingAi = crises.filter((c) => c.severityScore == null && !c.aiSummary).length;
    return {
      total: crises.length,
      critical,
      pendingAi,
      resources: resources.length,
    };
  }, [crises, resources]);

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Operations Dashboard</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Real-time crisis overview · auto-refresh every 15s
        </p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={Activity} label="Active Crises" value={stats.total} accent="text-foreground" />
        <StatCard icon={AlertTriangle} label="Critical (≥8)" value={stats.critical} accent="text-red-400" />
        <StatCard icon={Truck} label="Available Resources" value={stats.resources} accent="text-emerald-400" />
        <StatCard icon={Sparkles} label="Pending AI" value={stats.pendingAi} accent="text-amber-300" />
      </div>

      <div className="bg-card border rounded-lg overflow-hidden">
        <div className="p-4 border-b flex items-center justify-between">
          <h2 className="font-semibold">Active Crises</h2>
          {crisesQuery.isFetching && (
            <span className="text-xs text-muted-foreground">Refreshing…</span>
          )}
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-secondary/40 text-xs uppercase tracking-wider text-muted-foreground">
              <tr>
                <th className="text-left px-4 py-2.5 font-medium">Title</th>
                <th className="text-left px-4 py-2.5 font-medium">Type</th>
                <th className="text-left px-4 py-2.5 font-medium">Status</th>
                <th className="text-left px-4 py-2.5 font-medium">Severity</th>
                <th className="text-left px-4 py-2.5 font-medium">Reported</th>
                <th className="text-right px-4 py-2.5 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {crisesQuery.isLoading && (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-muted-foreground">
                    Loading…
                  </td>
                </tr>
              )}
              {crisesQuery.isError && (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-destructive">
                    Failed to load crises
                  </td>
                </tr>
              )}
              {!crisesQuery.isLoading && crises.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-4 py-8 text-center text-muted-foreground">
                    No active crises
                  </td>
                </tr>
              )}
              {crises.map((c) => (
                <tr key={c.id} className="hover:bg-secondary/30">
                  <td className="px-4 py-3 font-medium">{c.title}</td>
                  <td className="px-4 py-3">
                    <TypeBadge type={c.type} />
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge status={c.status} />
                  </td>
                  <td className="px-4 py-3">
                    <SeverityPill value={c.severityScore ?? c.severity} />
                  </td>
                  <td className="px-4 py-3 text-muted-foreground text-xs">
                    {new Date(c.reportedAt ?? c.createdAt ?? Date.now()).toLocaleString()}
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button
                      onClick={() => setSelected(c)}
                      className="rounded-md border px-3 py-1 text-xs font-medium hover:bg-secondary"
                    >
                      View
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {selected && <CrisisModal crisis={selected} onClose={() => setSelected(null)} />}
    </div>
  );
}

function StatCard({
  icon: Icon,
  label,
  value,
  accent,
}: {
  icon: typeof Activity;
  label: string;
  value: number;
  accent: string;
}) {
  return (
    <div className="bg-card border rounded-lg p-4">
      <div className="flex items-center justify-between mb-2">
        <span className="text-xs uppercase tracking-wider text-muted-foreground">{label}</span>
        <Icon className={`h-4 w-4 ${accent}`} />
      </div>
      <div className={`text-3xl font-bold tabular-nums ${accent}`}>{value}</div>
    </div>
  );
}