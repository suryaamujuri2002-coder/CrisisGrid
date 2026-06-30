import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { Loader2, Sparkles, X } from "lucide-react";
import type { Crisis, CrisisStatus } from "@/lib/types";
import { api } from "@/lib/api";
import { AI_SERVICE, CRISIS_SERVICE } from "@/lib/config";
import { useAuth } from "@/lib/auth";
import { SeverityPill, StatusBadge, TypeBadge } from "./badges";

interface Props {
  crisis: Crisis;
  onClose: () => void;
}

export function CrisisModal({ crisis, onClose }: Props) {
  const { isAdmin } = useAuth();
  const qc = useQueryClient();
  const [status, setStatus] = useState<CrisisStatus>(crisis.status);

  const statusMutation = useMutation({
    mutationFn: (newStatus: CrisisStatus) =>
      api(`${CRISIS_SERVICE}/api/v1/crisis/${crisis.id}/status`, {
        method: "PATCH",
        body: { status: newStatus },
      }),
    onSuccess: () => {
      toast.success("Status updated");
      qc.invalidateQueries({ queryKey: ["crisis"] });
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const aiMutation = useMutation({
    mutationFn: () =>
      api(`${AI_SERVICE}/api/v1/ai/analyze`, {
        method: "POST",
        body: {
          crisisId: crisis.id,
          title: crisis.title,
          description: crisis.description,
          latitude: crisis.latitude,
          longitude: crisis.longitude,
        },
      }),
    onSuccess: () => {
      toast.success("AI analysis triggered");
      qc.invalidateQueries({ queryKey: ["crisis"] });
    },
    onError: (e: Error) => toast.error(e.message),
  });

  const score = crisis.severityScore ?? crisis.severity ?? null;
  const hasAi = score != null || !!crisis.aiSummary;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4" onClick={onClose}>
      <div
        className="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-card border rounded-lg shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between p-5 border-b sticky top-0 bg-card">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <TypeBadge type={crisis.type} />
              <StatusBadge status={crisis.status} />
            </div>
            <h2 className="text-xl font-bold">{crisis.title}</h2>
          </div>
          <button onClick={onClose} className="p-1.5 rounded-md hover:bg-secondary">
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="p-5 space-y-5">
          <section>
            <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1.5">Description</h3>
            <p className="text-sm leading-relaxed whitespace-pre-wrap">{crisis.description}</p>
          </section>

          <section className="grid grid-cols-2 gap-4 text-sm">
            <div>
              <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1">Location</div>
              <div className="tabular-nums">{crisis.latitude.toFixed(4)}, {crisis.longitude.toFixed(4)}</div>
              {crisis.address && <div className="text-muted-foreground text-xs mt-0.5">{crisis.address}</div>}
            </div>
            <div>
              <div className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-1">Reported</div>
              <div>{new Date(crisis.reportedAt ?? crisis.createdAt ?? Date.now()).toLocaleString()}</div>
            </div>
          </section>

          <section className="border rounded-lg p-4 bg-secondary/30">
            <div className="flex items-center gap-2 mb-3">
              <Sparkles className="h-4 w-4 text-accent" />
              <h3 className="text-sm font-semibold">AI Analysis</h3>
            </div>
            {hasAi ? (
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <span className="text-xs uppercase tracking-wider text-muted-foreground">Severity</span>
                  <SeverityPill value={score} />
                  <span className="text-xs text-muted-foreground">/ 10</span>
                </div>
                {crisis.aiSummary && (
                  <p className="text-sm italic text-muted-foreground border-l-2 border-accent pl-3">
                    {crisis.aiSummary}
                  </p>
                )}
              </div>
            ) : (
              <div className="flex items-center gap-2 text-sm text-muted-foreground">
                <span className="inline-block h-2 w-2 rounded-full bg-accent animate-pulse" />
                AI analysis in progress…
              </div>
            )}
            {isAdmin && (
              <button
                onClick={() => aiMutation.mutate()}
                disabled={aiMutation.isPending}
                className="mt-3 inline-flex items-center gap-1.5 rounded-md border border-accent/40 bg-accent/10 px-3 py-1.5 text-xs font-medium text-accent hover:bg-accent/20 disabled:opacity-50"
              >
                {aiMutation.isPending && <Loader2 className="h-3 w-3 animate-spin" />}
                Re-run AI Analysis
              </button>
            )}
          </section>

          {isAdmin && (
            <section className="border rounded-lg p-4">
              <h3 className="text-sm font-semibold mb-2">Admin: Change Status</h3>
              <div className="flex items-center gap-2">
                <select
                  value={status}
                  onChange={(e) => setStatus(e.target.value as CrisisStatus)}
                  className="rounded-md border bg-input/40 px-3 py-1.5 text-sm"
                >
                  <option value="ACTIVE">ACTIVE</option>
                  <option value="RESOLVED">RESOLVED</option>
                  <option value="CLOSED">CLOSED</option>
                </select>
                <button
                  onClick={() => statusMutation.mutate(status)}
                  disabled={statusMutation.isPending || status === crisis.status}
                  className="rounded-md bg-primary px-3 py-1.5 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
                >
                  Apply
                </button>
              </div>
            </section>
          )}
        </div>
      </div>
    </div>
  );
}
