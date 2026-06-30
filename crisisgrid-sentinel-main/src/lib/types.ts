// ─── Crisis Service types ────────────────────────────────────────────────────

export type CrisisType =
  | "FIRE"
  | "FLOOD"
  | "MEDICAL"
  | "EARTHQUAKE"
  | "ACCIDENT"
  | "CHEMICAL"
  | "INFRASTRUCTURE"
  | "OTHER";

export type CrisisStatus = "PENDING" | "ACTIVE" | "RESOLVED" | "CLOSED";

/**
 * Matches CrisisReportResponse from crisis-service.
 *
 * Key fix: backend returns `crisisType` (not `type`).
 * The frontend normalises it into a `type` getter so all
 * existing components continue to work without changes.
 */
export interface Crisis {
  id: string;

  title: string;
  description: string;

  // Backend field name — always present in API responses
  crisisType?: CrisisType | null;

  // Convenience alias populated by normaliseCrisis() below.
  // Components use `crisis.type` so we keep this as well.
  type: CrisisType;

  status: CrisisStatus;

  severityScore?: number | null;
  /** Legacy alias — some components read this */
  severity?: number | null;

  aiSummary?: string | null;

  latitude: number;
  longitude: number;
  address?: string | null;

  reportedBy?: string | null;

  // Backend returns LocalDateTime serialised as array by default.
  // We always go through normaliseCrisis() which converts to ISO string.
  createdAt?: string | null;
  updatedAt?: string | null;

  /** Alias for createdAt — components use either name */
  reportedAt?: string | null;

  /** Present in nearby search results when backend returns it */
  distanceKm?: number | null;
}

/**
 * Call this on every object coming from the crisis-service API before
 * storing it in state / passing to components.
 *
 * Fixes:
 *  1. `crisisType` → `type`  (field name mismatch)
 *  2. LocalDateTime arrays   → ISO string   (Spring default serialisation)
 *  3. `createdAt`            → `reportedAt` alias
 */
export function normaliseCrisis(raw: Record<string, unknown>): Crisis {
  const crisisType = (raw.crisisType ?? raw.type ?? "OTHER") as CrisisType;

  return {
    ...(raw as unknown as Crisis),
    type: crisisType,
    crisisType: crisisType,
    createdAt: toIsoString(raw.createdAt),
    updatedAt: toIsoString(raw.updatedAt),
    reportedAt: toIsoString(raw.createdAt), // alias
  };
}

// ─── Resource Service types ──────────────────────────────────────────────────

export type ResourceType =
  | "AMBULANCE"
  | "FIRE_TRUCK"
  | "RESCUE_TEAM"
  | "POLICE"
  | "HELICOPTER"
  | "BOAT";

export type ResourceStatus = "AVAILABLE" | "DEPLOYED" | "MAINTENANCE" | "OFFLINE";

/**
 * Matches ResourceResponse from resource-service.
 *
 * Key fix: backend returns `currentLatitude`/`currentLongitude`,
 * not `latitude`/`longitude`. We normalise with normaliseResource().
 */
export interface Resource {
  id: string;
  name: string;
  type: ResourceType;
  status: ResourceStatus;

  // Raw backend field names
  currentLatitude?: number | null;
  currentLongitude?: number | null;

  // Normalised aliases used by components
  latitude: number;
  longitude: number;

  baseLocation?: string | null;
  capacity?: number | null;
  assignedCrisisId?: string | null;
  contactNumber?: string | null;
  description?: string | null;

  createdAt?: string | null;
  updatedAt?: string | null;
}

/**
 * Call this on every object from the resource-service API.
 *
 * Fixes:
 *  1. `currentLatitude`/`currentLongitude` → `latitude`/`longitude`
 *  2. LocalDateTime arrays → ISO string
 */
export function normaliseResource(raw: Record<string, unknown>): Resource {
  return {
    ...(raw as unknown as Resource),
    latitude: (raw.currentLatitude as number) ?? (raw.latitude as number) ?? 0,
    longitude: (raw.currentLongitude as number) ?? (raw.longitude as number) ?? 0,
    updatedAt: toIsoString(raw.updatedAt),
    createdAt: toIsoString(raw.createdAt),
  };
}

// ─── Pagination wrapper ──────────────────────────────────────────────────────

export interface PageResponse<T> {
  content: T[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
}

// ─── Helper ──────────────────────────────────────────────────────────────────

/**
 * Spring Boot serialises LocalDateTime as a number array by default
 * e.g. [2025, 6, 28, 14, 30, 0].  Convert to ISO string so
 * `new Date(value)` works in every component.
 */
function toIsoString(value: unknown): string | null {
  if (value == null) return null;
  if (typeof value === "string") return value;
  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value as number[];
    // month is 1-based from Java
    return new Date(year, month - 1, day, hour, minute, second).toISOString();
  }
  return String(value);
}