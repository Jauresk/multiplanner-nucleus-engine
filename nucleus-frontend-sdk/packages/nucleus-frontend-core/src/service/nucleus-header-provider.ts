import { NucleusHeaders } from "../headers/nucleus-headers";
import type { CorrelationIdFactory } from "../ids/correlation-id-factory";
import type { SessionIdStore } from "../ids/session-id-store";
import type { NucleusFrontendConfig } from "../model/nucleus-frontend-config";
import type { NucleusRequestContext } from "../model/nucleus-request-context";

export class NucleusHeaderProvider {
  constructor(
    private readonly config: NucleusFrontendConfig,
    private readonly correlationIdFactory: CorrelationIdFactory,
    private readonly sessionIdStore: SessionIdStore
  ) {}

  createContext(): NucleusRequestContext {
    const sessionId = this.getOrCreateSessionId();
    return {
      applicationCode: this.config.applicationCode,
      correlationId: this.correlationIdFactory.create(),
      sessionId,
      consentVersion: this.config.consentVersion
    };
  }

  createHeaders(context: NucleusRequestContext): Record<string, string> {
    const headers: Record<string, string> = {
      [NucleusHeaders.CORRELATION_ID]: context.correlationId,
      [NucleusHeaders.SESSION_ID]: context.sessionId
    };

    if (context.consentVersion) {
      headers[NucleusHeaders.CONSENT_VERSION] = context.consentVersion;
    }

    return headers;
  }

  private getOrCreateSessionId(): string {
    const existing = this.sessionIdStore.get();
    if (existing) return existing;

    const created = this.correlationIdFactory.create();
    this.sessionIdStore.set(created);
    return created;
  }
}
