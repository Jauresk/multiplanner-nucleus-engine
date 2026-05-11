import { createNucleusId } from "./nucleus-id";

export interface CorrelationIdFactory {
  create(): string;
}

export class RandomCorrelationIdFactory implements CorrelationIdFactory {
  create(): string {
    return createNucleusId();
  }
}
