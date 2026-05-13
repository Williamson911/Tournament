import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

interface AppEvent {
  type: string;
  payload?: unknown;
}

@Injectable({
  providedIn: 'root'
})
export class EventBusService {

  private bus = new Subject<AppEvent>();

  emit(type: string, payload?: unknown): void {
    this.bus.next({ type, payload });
  }

  on<T>(type: string): Observable<T> {
    return this.bus.asObservable().pipe(
      filter(event => event.type === type),
      map(event => event.payload as T)
    );
  }
}
