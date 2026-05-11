import { ErrorHandler, EnvironmentProviders, InjectionToken, makeEnvironmentProviders } from "@angular/core";
import type { NucleusFrontendConfig, NucleusObservationTransport } from "@multiplanner/nucleus-frontend-core";
import { NucleusFrontendService } from "../services/nucleus-frontend.service";
import { NucleusGlobalErrorHandler } from "../error/nucleus-global-error-handler";

export const NUCLEUS_FRONTEND_CONFIG = new InjectionToken<NucleusFrontendConfig>("NUCLEUS_FRONTEND_CONFIG");
export const NUCLEUS_OBSERVATION_TRANSPORT = new InjectionToken<NucleusObservationTransport>("NUCLEUS_OBSERVATION_TRANSPORT");

export function provideNucleusFrontend(
  config: NucleusFrontendConfig,
  transport: NucleusObservationTransport
): EnvironmentProviders {
  return makeEnvironmentProviders([
    { provide: NUCLEUS_FRONTEND_CONFIG, useValue: config },
    { provide: NUCLEUS_OBSERVATION_TRANSPORT, useValue: transport },
    NucleusFrontendService,
    { provide: ErrorHandler, useClass: NucleusGlobalErrorHandler }
  ]);
}
