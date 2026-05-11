export interface NucleusFrontendError {
  errorId: string;
  occurredAt: string;
  applicationCode: string;
  correlationId?: string;
  sessionId: string;
  message: string;
  stack?: string;
  source: "ANGULAR" | "REACT_NATIVE" | "UNKNOWN";
  metadata?: Record<string, string | number | boolean | null>;
}
