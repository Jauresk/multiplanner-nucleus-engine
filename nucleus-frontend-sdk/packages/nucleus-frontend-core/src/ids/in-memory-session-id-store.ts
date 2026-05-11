import type { SessionIdStore } from "./session-id-store";

export class InMemorySessionIdStore implements SessionIdStore {
  private sessionId: string | null = null;

  get(): string | null {
    return this.sessionId;
  }

  set(sessionId: string): void {
    this.sessionId = sessionId;
  }
}
