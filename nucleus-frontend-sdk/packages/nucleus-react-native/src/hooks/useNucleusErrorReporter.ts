import { useCallback } from "react";
import { createNucleusId } from "@multiplanner/nucleus-frontend-core";
import { useNucleus } from "./useNucleus";

export function useNucleusErrorReporter() {
  const { config, headerProvider, observationClient } = useNucleus();

  return useCallback(
    async (error: unknown) => {
      const context = headerProvider.createContext();
      const message = error instanceof Error ? error.message : String(error);
      const stack = error instanceof Error ? error.stack : undefined;

      await observationClient.recordFrontendError({
        errorId: createNucleusId(),
        occurredAt: new Date().toISOString(),
        applicationCode: config.applicationCode,
        correlationId: context.correlationId,
        sessionId: context.sessionId,
        message,
        stack,
        source: "REACT_NATIVE"
      });
    },
    [config.applicationCode, headerProvider, observationClient]
  );
}
