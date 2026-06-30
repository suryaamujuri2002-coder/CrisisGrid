import { TOKEN_KEY } from "./config";

export class ApiError extends Error {
  status: number;
  body: unknown;
  constructor(message: string, status: number, body: unknown) {
    super(message);
    this.status = status;
    this.body = body;
  }
}

let networkErrorHandler: ((msg: string) => void) | null = null;
let unauthorizedHandler: (() => void) | null = null;

export function setNetworkErrorHandler(fn: (msg: string) => void) {
  networkErrorHandler = fn;
}
export function setUnauthorizedHandler(fn: () => void) {
  unauthorizedHandler = fn;
}

interface ApiOptions extends Omit<RequestInit, "body"> {
  body?: unknown;
  auth?: boolean;
}

export async function api<T = unknown>(url: string, options: ApiOptions = {}): Promise<T> {
  const { body, auth = true, headers, ...rest } = options;
  const finalHeaders: Record<string, string> = {
    "Content-Type": "application/json",
    ...(headers as Record<string, string> | undefined),
  };
  if (auth && typeof window !== "undefined") {
    const token = window.localStorage.getItem(TOKEN_KEY);
    if (token) finalHeaders["Authorization"] = `Bearer ${token}`;
  }

  let response: Response;
  try {
    response = await fetch(url, {
      ...rest,
      headers: finalHeaders,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch (err) {
    const msg = "Could not reach backend — check services are running.";
    networkErrorHandler?.(msg);
    throw new ApiError(msg, 0, null);
  }

  const text = await response.text();
  let parsed: unknown = null;
  if (text) {
    try {
      parsed = JSON.parse(text);
    } catch {
      parsed = text;
    }
  }

  if (!response.ok) {
    if (response.status === 401) {
      unauthorizedHandler?.();
    }
    const message =
      (parsed && typeof parsed === "object" && "message" in parsed
        ? String((parsed as { message: unknown }).message)
        : null) ||
      (typeof parsed === "string" ? parsed : null) ||
      `Request failed (${response.status})`;
    throw new ApiError(message, response.status, parsed);
  }

  return parsed as T;
}
