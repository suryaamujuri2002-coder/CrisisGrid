import { useEffect, useState } from "react";
import { MapContainer, TileLayer, Marker, Popup, Circle, useMap } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import type { Crisis } from "@/lib/types";

// Fix default marker icons (bundlers break the relative URLs)
delete (L.Icon.Default.prototype as unknown as { _getIconUrl?: unknown })._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
  iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
});

function colorForSeverity(score: number | null | undefined): string {
  if (score == null) return "#64748b";
  if (score >= 9) return "#ef4444";
  if (score >= 7) return "#f97316";
  if (score >= 4) return "#f59e0b";
  return "#10b981";
}

function makeIcon(score: number | null | undefined): L.DivIcon {
  const color = colorForSeverity(score);
  const label = score != null ? Math.round(score) : "?";
  return L.divIcon({
    className: "crisis-marker",
    html: `<div style="background:${color};color:#0b0f17;font-weight:700;font-size:12px;width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center;border:2px solid #0b0f17;box-shadow:0 0 0 2px ${color},0 2px 6px rgba(0,0,0,.5)">${label}</div>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14],
  });
}

function FitBounds({ points }: { points: Array<[number, number]> }) {
  const map = useMap();
  useEffect(() => {
    if (points.length === 0) return;
    const bounds = L.latLngBounds(points);
    map.fitBounds(bounds, { padding: [40, 40], maxZoom: 14 });
  }, [points, map]);
  return null;
}

interface Props {
  center: [number, number];
  radiusKm: number;
  crises: Crisis[];
  onSelect: (c: Crisis) => void;
}

export function CrisisMap({ center, radiusKm, crises, onSelect }: Props) {
  const [mounted, setMounted] = useState(false);
  useEffect(() => setMounted(true), []);
  if (!mounted) {
    return <div className="h-[420px] rounded-lg border bg-secondary/30 animate-pulse" />;
  }

  const points: Array<[number, number]> = [
    center,
    ...crises.map((c) => [c.latitude, c.longitude] as [number, number]),
  ];

  return (
    <div className="h-[420px] rounded-lg border overflow-hidden">
      <MapContainer center={center} zoom={12} className="h-full w-full" scrollWheelZoom>
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        />
        <Circle
          center={center}
          radius={radiusKm * 1000}
          pathOptions={{ color: "#f59e0b", weight: 1, fillOpacity: 0.05 }}
        />
        <Marker position={center}>
          <Popup>Search origin</Popup>
        </Marker>
        {crises.map((c) => (
          <Marker
            key={c.id}
            position={[c.latitude, c.longitude]}
            icon={makeIcon(c.severityScore ?? c.severity)}
            eventHandlers={{ click: () => onSelect(c) }}
          >
            <Popup>
              <div className="text-sm">
                <div className="font-semibold mb-1">{c.title}</div>
                <div className="text-xs opacity-70 mb-2">{c.type} · {c.status}</div>
                <button
                  onClick={() => onSelect(c)}
                  className="text-xs font-medium text-red-600 hover:underline"
                >
                  View details →
                </button>
              </div>
            </Popup>
          </Marker>
        ))}
        <FitBounds points={points} />
      </MapContainer>
    </div>
  );
}
