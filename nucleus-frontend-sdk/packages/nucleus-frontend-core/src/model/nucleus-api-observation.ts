export interface NucleusApiObservation {
  observationId: string;
  occurredAt: string;
  applicationCode: string;
  method: string;
  url: string;
  correlationId: string;
  sessionId: string;
  status?: number;
  clientDurationMs: number;
  serverTiming?: string;
  outcome: "SUCCESS" | "ERROR";
}
