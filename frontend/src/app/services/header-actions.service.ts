import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class HeaderActionsService {
  // Optional refresh handler, set by active page
  private readonly _refresh = signal<(() => void) | null>(null);

  refresh = this._refresh; // expose as read-only signal

  setRefresh(handler: (() => void) | null) {
    this._refresh.set(handler);
  }

  triggerRefresh() {
    const h = this._refresh();
    if (h) h();
  }
}
