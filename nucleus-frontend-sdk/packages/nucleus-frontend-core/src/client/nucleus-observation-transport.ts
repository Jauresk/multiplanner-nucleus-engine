import type { NucleusApiObservation } from "../model/nucleus-api-observation";
import type { NucleusFrontendError } from "../model/nucleus-frontend-error";

export interface NucleusObservationTransport {
  sendApiObservation(observation: NucleusApiObservation): Promise<void>;
  sendFrontendError(error: NucleusFrontendError): Promise<void>;
}
