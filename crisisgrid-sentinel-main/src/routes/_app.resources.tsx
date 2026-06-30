import { createFileRoute } from "@tanstack/react-router";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { toast } from "sonner";
import { Plus } from "lucide-react";
import { api } from "@/lib/api";
import { RESOURCE_SERVICE } from "@/lib/config";
import type { PageResponse, Resource, ResourceType } from "@/lib/types";
import { normaliseResource } from "@/lib/types";
import { ResourceStatusBadge, ResourceTypeBadge } from "@/components/badges";
import { useAuth } from "@/lib/auth";

export const Route = createFileRoute("/_app/resources")({
  head: () => ({ meta: [{ title: "Resources — CrisisGrid" }] }),
  component: ResourcesPage,
});

const TABS = [
  { key: "available", label: "Available", url: `${RESOURCE_SERVICE}/api/v1/resources/available` },
  { key: "deployed", label: "Deployed", url: `${RESOURCE_SERVICE}/api/v1/resources/deployed` },
  { key: "all", label: "All", url: `${RESOURCE_SERVICE}/api/v1/resources?page=0&size=20` },
] as const;

const TYPES: ResourceType[] = [
  "AMBULANCE",
  "FIRE_TRUCK",
  "RESCUE_TEAM",
  "POLICE",
  "HELICOPTER",
  "BOAT",
];

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

function ResourcesPage() {
  const { isAdmin } = useAuth();
  const [tab, setTab] = useState<(typeof TABS)[number]["key"]>("available");
  const [typeFilter, setTypeFilter] = useState<"ALL" | ResourceType>("ALL");
  const [showAdd, setShowAdd] = useState(false);

  const active = TABS.find((t) => t.key === tab)!;

  const query = useQuery({
    queryKey: ["resources", tab],
    queryFn: () => api<unknown>(active.url),
  });

  // FIX: normalise responses so currentLatitude/currentLongitude → latitude/longitude
  const resources = useMemo(() => {
    const list = extractList<Resource>(query.data, normaliseResource);
    return typeFilter === "ALL" ? list : list.filter((r) => r.type === typeFilter);
  }, [query.data, typeFilter]);

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Resources</h1>
          <p className="text-sm text-muted-foreground mt-1">Emergency response assets and units.</p>
        </div>
        {isAdmin && (
          <button
            onClick={() => setShowAdd(true)}
            className="inline-flex items-center gap-1.5 rounded-md bg-primary px-3 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
          >
            <Plus className="h-4 w-4" /> Add Resource
          </button>
        )}
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex gap-1 bg-card border rounded-lg p-1">
          {TABS.map((t) => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`rounded-md px-3 py-1.5 text-sm font-medium ${
                tab === t.key
                  ? "bg-primary text-primary-foreground"
                  : "text-muted-foreground hover:bg-secondary"
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
        <select
          value={typeFilter}
          onChange={(e) => setTypeFilter(e.target.value as "ALL" | ResourceType)}
          className="rounded-md border bg-input/40 px-3 py-1.5 text-sm"
        >
          <option value="ALL">All Types</option>
          {TYPES.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
        {query.isLoading && (
          <p className="text-sm text-muted-foreground col-span-full">Loading…</p>
        )}
        {!query.isLoading && resources.length === 0 && (
          <p className="text-sm text-muted-foreground col-span-full">No resources.</p>
        )}
        {resources.map((r) => (
          <div key={r.id} className="bg-card border rounded-lg p-4">
            <div className="flex items-start justify-between mb-2">
              <ResourceTypeBadge type={r.type} />
              <ResourceStatusBadge status={r.status} />
            </div>
            <h3 className="font-semibold mb-1">{r.name}</h3>
            {/* FIX: uses normalised latitude/longitude, not currentLatitude/currentLongitude */}
            <div className="text-xs text-muted-foreground tabular-nums">
              {r.latitude?.toFixed(3)}, {r.longitude?.toFixed(3)}
            </div>
            {r.updatedAt && (
              <div className="text-[10px] uppercase tracking-wider text-muted-foreground mt-2">
                Updated {new Date(r.updatedAt).toLocaleString()}
              </div>
            )}
          </div>
        ))}
      </div>

      {showAdd && <AddResourceModal onClose={() => setShowAdd(false)} />}
    </div>
  );
}

function AddResourceModal({ onClose }: { onClose: () => void }) {
  const qc = useQueryClient();
  const [name, setName] = useState("");
  const [type, setType] = useState<ResourceType>("AMBULANCE");
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [capacity, setCapacity] = useState("1");
  const [contactNumber, setContactNumber] = useState("");

  const mutation = useMutation({
    mutationFn: () =>
      api(`${RESOURCE_SERVICE}/api/v1/resources`, {
        method: "POST",
        body: {
          name,
          type,
          status: "AVAILABLE",
          // FIX: ResourceRequest expects `currentLatitude`/`currentLongitude`,
          // not `latitude`/`longitude`. Using the wrong names means the backend
          // saves null coordinates and validation may reject the request.
          currentLatitude: parseFloat(latitude),
          currentLongitude: parseFloat(longitude),
          // FIX: `capacity` is @NotNull in ResourceRequest — omitting it causes
          // a 400 Bad Request with "Capacity is required".
          capacity: parseInt(capacity, 10),
          // Include contactNumber only if the user filled it in, otherwise
          // the @Pattern validator rejects an empty string.
          contactNumber: contactNumber.trim() || undefined,
        },
      }),
    onSuccess: () => {
      toast.success("Resource added");
      qc.invalidateQueries({ queryKey: ["resources"] });
      onClose();
    },
    onError: (e: Error) => toast.error(e.message),
  });

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4"
      onClick={onClose}
    >
      <form
        onClick={(e) => e.stopPropagation()}
        onSubmit={(e) => {
          e.preventDefault();
          mutation.mutate();
        }}
        className="w-full max-w-md bg-card border rounded-lg p-6 space-y-4"
      >
        <h2 className="text-lg font-semibold">Add Resource</h2>

        <input
          required
          placeholder="Name (e.g. Ambulance Unit 3)"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm"
        />

        <select
          value={type}
          onChange={(e) => setType(e.target.value as ResourceType)}
          className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm"
        >
          {TYPES.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>

        <div className="grid grid-cols-2 gap-3">
          <input
            required
            type="number"
            step="any"
            placeholder="Latitude"
            value={latitude}
            onChange={(e) => setLatitude(e.target.value)}
            className="rounded-md border bg-input/40 px-3 py-2 text-sm"
          />
          <input
            required
            type="number"
            step="any"
            placeholder="Longitude"
            value={longitude}
            onChange={(e) => setLongitude(e.target.value)}
            className="rounded-md border bg-input/40 px-3 py-2 text-sm"
          />
        </div>

        {/* FIX: capacity is required by the backend */}
        <div>
          <label className="text-xs text-muted-foreground block mb-1">
            Capacity (crew / unit count)
          </label>
          <input
            required
            type="number"
            min={1}
            value={capacity}
            onChange={(e) => setCapacity(e.target.value)}
            className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm"
          />
        </div>

        <div>
          <label className="text-xs text-muted-foreground block mb-1">
            Contact number (optional, e.g. +911234567890)
          </label>
          <input
            type="tel"
            placeholder="+91XXXXXXXXXX"
            value={contactNumber}
            onChange={(e) => setContactNumber(e.target.value)}
            className="w-full rounded-md border bg-input/40 px-3 py-2 text-sm"
          />
        </div>

        <div className="flex gap-2 justify-end pt-2">
          <button
            type="button"
            onClick={onClose}
            className="rounded-md border px-3 py-1.5 text-sm hover:bg-secondary"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={mutation.isPending}
            className="rounded-md bg-primary px-3 py-1.5 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
          >
            {mutation.isPending ? "Adding…" : "Add"}
          </button>
        </div>
      </form>
    </div>
  );
}