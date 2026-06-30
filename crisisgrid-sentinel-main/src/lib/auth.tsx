import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { TOKEN_KEY } from "./config";

export interface JwtPayload {
  sub?: string;
  email?: string;
  roles?: string[];
  authorities?: string[];
  exp?: number;
  [key: string]: unknown;
}

function decodeJwt(token: string): JwtPayload | null {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    const payload = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const padded = payload + "=".repeat((4 - (payload.length % 4)) % 4);
    const json = atob(padded);
    return JSON.parse(decodeURIComponent(escape(json)));
  } catch {
    try {
      return JSON.parse(atob(token.split(".")[1]));
    } catch {
      return null;
    }
  }
}

export function getRolesFromPayload(payload: JwtPayload | null): string[] {
  if (!payload) return [];
  const collected: string[] = [];
  if (Array.isArray(payload.roles)) collected.push(...payload.roles.map(String));
  if (Array.isArray(payload.authorities)) collected.push(...payload.authorities.map(String));
  const extra = (payload as Record<string, unknown>)["role"];
  if (typeof extra === "string") collected.push(extra);
  return collected;
}

interface AuthContextValue {
  token: string | null;
  payload: JwtPayload | null;
  email: string | null;
  roles: string[];
  isAdmin: boolean;
  isAuthenticated: boolean;
  login: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => {
    if (typeof window === "undefined") return null;
    return window.localStorage.getItem(TOKEN_KEY);
  });

  useEffect(() => {
    const onStorage = (e: StorageEvent) => {
      if (e.key === TOKEN_KEY) setToken(e.newValue);
    };
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  const login = useCallback((newToken: string) => {
    window.localStorage.setItem(TOKEN_KEY, newToken);
    setToken(newToken);
  }, []);

  const logout = useCallback(() => {
    window.localStorage.removeItem(TOKEN_KEY);
    setToken(null);
  }, []);

  const value = useMemo<AuthContextValue>(() => {
    const payload = token ? decodeJwt(token) : null;
    const roles = getRolesFromPayload(payload);
    const isAdmin = roles.some((r) => r.toUpperCase().includes("ADMIN"));
    const email =
      (payload?.email as string | undefined) ??
      (typeof payload?.sub === "string" && payload.sub.includes("@") ? payload.sub : null);
    return {
      token,
      payload,
      email,
      roles,
      isAdmin,
      isAuthenticated: !!token,
      login,
      logout,
    };
  }, [token, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
