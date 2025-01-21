import { Injectable } from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoaderService {
  private isChatsLoadingSubject = new BehaviorSubject<boolean>(true); // Domyślnie true
  isChatsLoading$ = this.isChatsLoadingSubject.asObservable();

  setLoadingState(isLoading: boolean): void {
    this.isChatsLoadingSubject.next(isLoading);
  }
}
