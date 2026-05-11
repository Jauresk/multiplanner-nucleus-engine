export function createNucleusId(): string {
  const cryptoApi = globalThis.crypto as Crypto | undefined;
  if (cryptoApi && "randomUUID" in cryptoApi) {
    return cryptoApi.randomUUID();
  }

  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 12)}`;
}
