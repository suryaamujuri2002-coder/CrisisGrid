import { createFileRoute, Link, Outlet, useNavigate, useRouterState } from "@tanstack/react-router";
import { useEffect, useState } from "react";
import {
  Bell,
  FileText,
  LayoutDashboard,
  LogOut,
  MapPin,
  Settings as SettingsIcon,
  Shield,
  Siren,
  Truck,
} from "lucide-react";
import { useAuth } from "@/lib/auth";
import { setNetworkErrorHandler, setUnauthorizedHandler } from "@/lib/api";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/_app")({
  component: AppLayout,
});

const NAV = [
  { to: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { to: "/report", label: "Report Crisis", icon: Siren },
  { to: "/nearby", label: "Nearby Map", icon: MapPin },
  { to: "/resources", label: "Resources", icon: Truck },
  { to: "/my-reports", label: "My Reports", icon: Bell },
  { to: "/settings", label: "Settings", icon: SettingsIcon },
] as const;

function AppLayout() {
  const { isAuthenticated, email, logout } = useAuth();
  const navigate = useNavigate();
  const pathname = useRouterState({ select: (s) => s.location.pathname });
  const [netError, setNetError] = useState<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate({ to: "/login" });
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    setNetworkErrorHandler((m) => setNetError(m));
    setUnauthorizedHandler(() => {
      logout();
      navigate({ to: "/login" });
    });
  }, [logout, navigate]);

  if (!isAuthenticated) return null;

  return (
    <div className="min-h-screen flex bg-background text-foreground">
      <aside className="w-60 bg-sidebar text-sidebar-foreground border-r border-sidebar-border flex flex-col">
        <div className="p-5 border-b border-sidebar-border flex items-center gap-2.5">
          <div className="h-9 w-9 rounded-md bg-primary flex items-center justify-center">
            <Shield className="h-5 w-5 text-primary-foreground" />
          </div>
          <div>
            <div className="font-bold text-sm tracking-tight">CrisisGrid</div>
            <div className="text-[10px] uppercase tracking-wider text-muted-foreground">Ops Console</div>
          </div>
        </div>
        <nav className="flex-1 p-3 space-y-1">
          {NAV.map((item) => {
            const active = pathname === item.to || pathname.startsWith(item.to + "/");
            const Icon = item.icon;
            return (
              <Link
                key={item.to}
                to={item.to}
                className={cn(
                  "flex items-center gap-2.5 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                  active
                    ? "bg-primary/15 text-primary border border-primary/30"
                    : "text-sidebar-foreground/80 hover:bg-secondary/50 hover:text-sidebar-foreground",
                )}
              >
                <Icon className="h-4 w-4" />
                {item.label}
              </Link>
            );
          })}
        </nav>
        <div className="p-3 border-t border-sidebar-border text-xs text-muted-foreground">
          v1.0 · Operational
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-w-0">
        <header className="h-14 border-b bg-card/40 backdrop-blur flex items-center justify-between px-6">
          <div className="flex items-center gap-2 text-sm">
            <span className="inline-block h-2 w-2 rounded-full bg-emerald-400 animate-pulse" />
            <span className="text-muted-foreground">System Online</span>
          </div>
          <div className="flex items-center gap-3 text-sm">
            <span className="text-muted-foreground">{email ?? "operator"}</span>
            <button
              onClick={() => {
                logout();
                navigate({ to: "/login" });
              }}
              className="inline-flex items-center gap-1.5 rounded-md border px-3 py-1.5 text-xs font-medium hover:bg-secondary"
            >
              <LogOut className="h-3.5 w-3.5" />
              Sign out
            </button>
          </div>
        </header>

        {netError && (
          <div className="bg-destructive/20 border-b border-destructive/40 text-destructive-foreground px-6 py-2 text-sm flex items-center justify-between">
            <span>{netError}</span>
            <button onClick={() => setNetError(null)} className="text-xs hover:underline">Dismiss</button>
          </div>
        )}

        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

// Suppress unused-import lint for FileText
void FileText;
