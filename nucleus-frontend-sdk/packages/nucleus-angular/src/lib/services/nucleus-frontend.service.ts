import { Inject, Injectable } from "@angular/core";
import {
  InMemorySessionIdStore,
  NucleusHeaderProvider,
  NucleusObservationClient,
  RandomCorrelationIdFactory,
  type NucleusFrontendError,
  type NucleusRequestContext
} from "@multiplanner/nucleus-frontend-core";
import { NUCLEUS_FRONTEND_CONFIG, NUCLEUS_OBSERVATION_TRANSPORT } from "../providers/provide-nucleus-frontend";
import type { NucleusFrontendConfig, NucleusObservationTransport } from "@multiplanner/nucleus-frontend-core";

@Injectable()
export class NucleusFrontendService {
  private readonly idFactory = new RandomCorrelationIdFactory();
  private readonly headerProvider: NucleusHeaderProvider;
  private readonly observationClient: NucleusObservationClient;

  constructor(
    @Inject(NUCLEUS_FRONTEND_CONFIG) config: NucleusFrontendConfig,
    @Inject(NUCLEUS_OBSERVATION_TRANSPORT) transport: NucleusObservationTransport
  ) {
    this.headerProvider = new NucleusHeaderProvider(config, this.idFactory, new InMemorySessionIdStore());
    this.observationClient = new NucleusObservationClient(transport);
  }

  createRequestContext(): NucleusRequestContext {
    return this.headerProvider.createContext();
  }

  createHeaders(context: NucleusRequestContext): Record<string, string> {
    return this.headerProvider.createHeaders(context);
  }

  async recordFrontendError(error: NucleusFrontendError): Promise<void> {
    await this.observationClient.recordFrontendError(error);
  }

  get client(): NucleusObservationClient {
    return this.observationClient;
  }
}
