import { cn } from "@/lib/utils";
import type { CrisisStatus, CrisisType, ResourceStatus, ResourceType } from "@/lib/types";

export function SeverityPill({ value }: { value: number | null | undefined }) {
  if (value == null) return <span className="text-xs text-muted-foreground">—</span>;
  const v = Math.round(value);
  let color = "bg-emerald-500/20 text-emerald-300 border-emerald-500/40";
  if (v >= 9) color = "bg-red-500/25 text-red-300 border-red-500/50";
  else if (v >= 7) color = "bg-orange-500/25 text-orange-300 border-orange-500/50";
  else if (v >= 4) color = "bg-amber-500/25 text-amber-300 border-amber-500/50";
  return (
    <span className={cn("inline-flex items-center justify-center rounded-full border px-2.5 py-0.5 text-xs font-semibold tabular-nums", color)}>
      {v}
    </span>
  );
}

const STATUS_COLORS: Record<CrisisStatus, string> = {
  PENDING: "bg-slate-500/20 text-slate-300 border-slate-500/40",
  ACTIVE: "bg-amber-500/20 text-amber-300 border-amber-500/40",
  RESOLVED: "bg-emerald-500/20 text-emerald-300 border-emerald-500/40",
  CLOSED: "bg-slate-700/40 text-slate-400 border-slate-600/40",
};

export function StatusBadge({ status }: { status: CrisisStatus }) {
  return (
    <span className={cn("inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium uppercase tracking-wide", STATUS_COLORS[status] ?? STATUS_COLORS.PENDING)}>
      {status}
    </span>
  );
}

const TYPE_COLORS: Record<CrisisType, string> = {
  FIRE: "bg-red-500/20 text-red-300 border-red-500/40",
  FLOOD: "bg-blue-500/20 text-blue-300 border-blue-500/40",
  MEDICAL: "bg-pink-500/20 text-pink-300 border-pink-500/40",
  EARTHQUAKE: "bg-orange-500/20 text-orange-300 border-orange-500/40",
  ACCIDENT: "bg-yellow-500/20 text-yellow-300 border-yellow-500/40",
  CHEMICAL: "bg-lime-500/20 text-lime-300 border-lime-500/40",
  INFRASTRUCTURE: "bg-indigo-500/20 text-indigo-300 border-indigo-500/40",
  OTHER: "bg-slate-500/20 text-slate-300 border-slate-500/40",
};

export function TypeBadge({ type }: { type: CrisisType }) {
  return (
    <span className={cn("inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium", TYPE_COLORS[type] ?? TYPE_COLORS.OTHER)}>
      {type}
    </span>
  );
}

const RES_STATUS: Record<ResourceStatus, string> = {
  AVAILABLE: "bg-emerald-500/20 text-emerald-300 border-emerald-500/40",
  DEPLOYED: "bg-amber-500/20 text-amber-300 border-amber-500/40",
  MAINTENANCE: "bg-blue-500/20 text-blue-300 border-blue-500/40",
  OFFLINE: "bg-slate-500/20 text-slate-300 border-slate-500/40",
};

export function ResourceStatusBadge({ status }: { status: ResourceStatus }) {
  return (
    <span className={cn("inline-flex items-center rounded-md border px-2 py-0.5 text-xs font-medium uppercase", RES_STATUS[status] ?? RES_STATUS.OFFLINE)}>
      {status}
    </span>
  );
}

const RES_LABEL: Record<ResourceType, string> = {
  AMBULANCE: "Ambulance",
  FIRE_TRUCK: "Fire Truck",
  RESCUE_TEAM: "Rescue Team",
  POLICE: "Police",
  HELICOPTER: "Helicopter",
  BOAT: "Boat",
};

export function ResourceTypeBadge({ type }: { type: ResourceType }) {
  return (
    <span className="inline-flex items-center rounded-md border border-border bg-secondary/50 px-2 py-0.5 text-xs font-medium">
      {RES_LABEL[type] ?? type}
    </span>
  );
}
