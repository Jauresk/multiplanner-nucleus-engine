import { ErrorHandler, Injectable } from "@angular/core";
import { createNucleusId } from "@multiplanner/nucleus-frontend-core";
import { NucleusFrontendService } from "../services/nucleus-frontend.service";

@Injectable()
export class NucleusGlobalErrorHandler implements ErrorHandler {
  constructor(private readonly nucleus: NucleusFrontendService) {}

  handleError(error: unknown): void {
    const message = error instanceof Error ? error.message : String(error);
    const stack = error instanceof Error ? error.stack : undefined;
    const context = this.nucleus.createRequestContext();

    void this.nucleus.recordFrontendError({
      errorId: createNucleusId(),
      occurredAt: new Date().toISOString(),
      applicationCode: context.applicationCode,
      correlationId: context.correlationId,
      sessionId: context.sessionId,
      message,
      stack,
      source: "ANGULAR"
    });

    console.error(error);
  }
}
