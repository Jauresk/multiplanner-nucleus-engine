export interface NucleusRequestContext {
  applicationCode: string;
  correlationId: string;
  sessionId: string;
  consentVersion?: string;
}
