export interface SessionIdStore {
  get(): string | null;
  set(sessionId: string): void;
}
