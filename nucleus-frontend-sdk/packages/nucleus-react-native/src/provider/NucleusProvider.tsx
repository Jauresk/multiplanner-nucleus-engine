import React, { createContext, useMemo } from "react";
import {
  InMemorySessionIdStore,
  NucleusHeaderProvider,
  NucleusObservationClient,
  RandomCorrelationIdFactory,
  type NucleusFrontendConfig,
  type NucleusObservationTransport
} from "@multiplanner/nucleus-frontend-core";

export interface NucleusProviderValue {
  config: NucleusFrontendConfig;
  headerProvider: NucleusHeaderProvider;
  observationClient: NucleusObservationClient;
}

export const NucleusContext = createContext<NucleusProviderValue | null>(null);

export function NucleusProvider({
  config,
  transport,
  children
}: {
  config: NucleusFrontendConfig;
  transport: NucleusObservationTransport;
  children: React.ReactNode;
}) {
  const value = useMemo<NucleusProviderValue>(() => {
    const factory = new RandomCorrelationIdFactory();
    return {
      config,
      headerProvider: new NucleusHeaderProvider(config, factory, new InMemorySessionIdStore()),
      observationClient: new NucleusObservationClient(transport)
    };
  }, [config, transport]);

  return <NucleusContext.Provider value={value}>{children}</NucleusContext.Provider>;
}
