import { NucleusHeaders, createNucleusId } from "@multiplanner/nucleus-frontend-core";
import { useNucleus } from "../hooks/useNucleus";

export function useNucleusFetch() {
  const { config, headerProvider, observationClient } = useNucleus();

  return async (input: RequestInfo | URL, init: RequestInit = {}) => {
    const context = headerProvider.createContext();
    const headers = {
      ...headerProvider.createHeaders(context),
      ...(init.headers ?? {})
    };
    const start = Date.now();

    try {
      const response = await fetch(input, { ...init, headers });

      await observationClient.recordApiObservation({
        observationId: createNucleusId(),
        occurredAt: new Date().toISOString(),
        applicationCode: config.applicationCode,
        method: init.method ?? "GET",
        url: String(input),
        correlationId: context.correlationId,
        sessionId: context.sessionId,
        status: response.status,
        clientDurationMs: Date.now() - start,
        serverTiming: response.headers.get(NucleusHeaders.REQUEST_TIMING) ?? undefined,
        outcome: response.ok ? "SUCCESS" : "ERROR"
      });

      return response;
    } catch (error) {
      await observationClient.recordApiObservation({
        observationId: createNucleusId(),
        occurredAt: new Date().toISOString(),
        applicationCode: config.applicationCode,
        method: init.method ?? "GET",
        url: String(input),
        correlationId: context.correlationId,
        sessionId: context.sessionId,
        clientDurationMs: Date.now() - start,
        outcome: "ERROR"
      });
      throw error;
    }
  };
}
