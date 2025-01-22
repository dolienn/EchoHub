import {ElementRef, Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ScrollerService {

  constructor() { }

  scrollToBottom(messagesRef: ElementRef): void {
    if (messagesRef?.nativeElement) {
      const messages = messagesRef.nativeElement;
      messages.scrollTop = messages.scrollHeight;
    }
  }
}
