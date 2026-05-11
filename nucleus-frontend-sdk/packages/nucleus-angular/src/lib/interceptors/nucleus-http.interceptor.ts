import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { NucleusHeaders, createNucleusId } from "@multiplanner/nucleus-frontend-core";
import { tap } from "rxjs";
import { NucleusFrontendService } from "../services/nucleus-frontend.service";

export const nucleusHttpInterceptor: HttpInterceptorFn = (request, next) => {
  const nucleus = inject(NucleusFrontendService);
  const context = nucleus.createRequestContext();
  const headers = nucleus.createHeaders(context);
  const start = performance.now();

  const nextRequest = request.clone({
    setHeaders: headers
  });

  return next(nextRequest).pipe(
    tap({
      next: async (response) => {
        await nucleus.client.recordApiObservation({
          observationId: createNucleusId(),
          occurredAt: new Date().toISOString(),
          applicationCode: context.applicationCode,
          method: request.method,
          url: request.url,
          correlationId: context.correlationId,
          sessionId: context.sessionId,
          status: response.status,
          clientDurationMs: Math.round(performance.now() - start),
          serverTiming: response.headers.get(NucleusHeaders.REQUEST_TIMING) ?? undefined,
          outcome: "SUCCESS"
        });
      },
      error: async () => {
        await nucleus.client.recordApiObservation({
          observationId: createNucleusId(),
          occurredAt: new Date().toISOString(),
          applicationCode: context.applicationCode,
          method: request.method,
          url: request.url,
          correlationId: context.correlationId,
          sessionId: context.sessionId,
          clientDurationMs: Math.round(performance.now() - start),
          outcome: "ERROR"
        });
      }
    })
  );
};
