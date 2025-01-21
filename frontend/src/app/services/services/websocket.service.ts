import { Injectable } from '@angular/core';
import * as Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import { environment } from '../../../environments/environment';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class WebSocketService {
  private socketClient: any = null;
  private notificationSubject = new Subject<Notification>();

  constructor() {}

  connect(userId: string, token: string): void {
    const ws = new SockJS(`${environment.apiBaseUrl}/ws`);
    this.socketClient = Stomp.over(ws);
    const subUrl = `/user/${userId}/chat`;

    this.socketClient.connect(
      { Authorization: `Bearer ${token}` },
      () => {
        this.socketClient.subscribe(
          subUrl,
          (message: any) => {
            const notification: Notification = JSON.parse(message.body);
            this.notificationSubject.next(notification);
          },
          () => console.error('Error while connecting to WebSocket')
        );
      }
    );
  }

  disconnect(): void {
    if (this.socketClient) {
      this.socketClient.disconnect();
      this.socketClient = null;
    }
  }

  getNotifications() {
    return this.notificationSubject.asObservable();
  }
}
