import { createFileRoute } from "@tanstack/react-router";
import { useAuth } from "@/lib/auth";

export const Route = createFileRoute("/_app/settings")({
  head: () => ({ meta: [{ title: "Settings — CrisisGrid" }] }),
  component: SettingsPage,
});

function SettingsPage() {
  const { email, roles, isAdmin } = useAuth();
  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold tracking-tight">Settings</h1>
        <p className="text-sm text-muted-foreground mt-1">Account info and preferences (coming soon).</p>
      </div>
      <div className="bg-card border rounded-lg p-5 space-y-3">
        <Row label="Email" value={email ?? "—"} />
        <Row label="Roles" value={roles.length ? roles.join(", ") : "—"} />
        <Row label="Admin" value={isAdmin ? "Yes" : "No"} />
      </div>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between text-sm">
      <span className="text-muted-foreground">{label}</span>
      <span className="font-medium">{value}</span>
    </div>
  );
}
