import type { NucleusApiObservation } from "../model/nucleus-api-observation";
import type { NucleusFrontendError } from "../model/nucleus-frontend-error";
import type { NucleusObservationTransport } from "./nucleus-observation-transport";

export class NucleusObservationClient {
  constructor(private readonly transport: NucleusObservationTransport) {}

  async recordApiObservation(observation: NucleusApiObservation): Promise<void> {
    await this.transport.sendApiObservation(observation);
  }

  async recordFrontendError(error: NucleusFrontendError): Promise<void> {
    await this.transport.sendFrontendError(error);
  }
}
