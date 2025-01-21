import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Notification} from "./notification";
import {ChatResponse} from "../../services/models/chat-response";
import {ChatService} from "../../services/services/chat.service";
import {KeycloakService} from "../../utils/keycloak/keycloak.service";
import {MessageResponse} from "../../services/models/message-response";
import {MessageRequest} from "../../services/models/message-request";
import {MessageService} from "../../services/services/message.service";
import {PickerComponent} from "@ctrl/ngx-emoji-mart";
import {DatePipe} from "@angular/common";
import {ChatListComponent} from "../../components/chat-list/chat-list.component";
import {FormsModule} from "@angular/forms";
import {EmojiData} from "@ctrl/ngx-emoji-mart/ngx-emoji";
import {WebSocketService} from "../../services/services/websocket.service";

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [
    PickerComponent,
    DatePipe,
    ChatListComponent,
    FormsModule
  ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent implements OnInit, OnDestroy {
  @ViewChild('messages') messagesRef!: ElementRef;

  selectedChat: ChatResponse = {};
  chats: Array<ChatResponse> = [];
  chatMessages: Array<MessageResponse> = [];
  socketClient: any = null;
  messageContent: string = '';
  showEmojis = false;
  private notificationSubscription: any;

  constructor(
    private chatService: ChatService,
    private messageService: MessageService,
    private keycloakService: KeycloakService,
    private webSocketService: WebSocketService
  ) {
  }

  ngOnInit(): void {
    this.initWebSocket();
    this.loadChats();
    setTimeout(() => this.scrollToBottom(), 1);
  }

  ngOnDestroy(): void {
    this.webSocketService.disconnect();
    this.notificationSubscription.unsubscribe();
  }

  chatSelected(chatResponse: ChatResponse): void {
    this.selectedChat = chatResponse;
    this.loadMessages(chatResponse.id!);
    this.markMessagesAsSeen();
  }

  markMessagesAsSeen(): void {
    if (this.selectedChat.id) {
      this.messageService.setMessagesToSeen({ 'chat-id': this.selectedChat.id }).subscribe();
      this.selectedChat.unreadCount = 0;
    }
  }

  isSelfMessage(message: MessageResponse): boolean {
    return message.senderId === this.keycloakService.userId;
  }

  keyDown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      this.sendMessage();
    }
  }

  sendMessage(): void {
    if (this.messageContent.trim()) {
      const messageRequest: MessageRequest = this.createMessageRequest();
      this.messageService.saveMessage({ body: messageRequest }).subscribe({
        next: () => this.onMessageSent(),
        error: (err) => console.error('Error sending message', err)
      });
    }
  }

  onSelectEmojis(emojiSelected: any) {
    const emoji: EmojiData = emojiSelected.emoji;
    this.messageContent += emoji.native;
  }

  uploadMedia(target: EventTarget | null): void {
    const file = this.extractFileFromTarget(target);
    if (file) {
      const reader = new FileReader();
      reader.onload = () => this.uploadMediaContent(reader.result, file);
      reader.readAsDataURL(file);
    }
  }

  logout(): void {
    this.keycloakService.logout();
  }

  userProfile(): void {
    this.keycloakService.accountManagement();
  }

  scrollToBottom() {
    if (this.messagesRef?.nativeElement) {
      const messages = this.messagesRef.nativeElement;
      messages.scrollTop = messages.scrollHeight;
    }
  }

  private initWebSocket(): void {
    const userId = this.keycloakService.keycloak.tokenParsed?.sub;
    const token = this.keycloakService.keycloak.token;

    if (userId && token) {
      this.webSocketService.connect(userId, token);

      this.webSocketService.getNotifications().subscribe((notification: any) => {
        this.handleNotification(notification);
      });
    }
  }

  private handleNotification(notification: Notification) {
    if (!notification) return;
    if (this.selectedChat && this.selectedChat.id === notification.chatId) {
      this.handleChatNotification(notification);
    } else {
      this.handleGeneralNotification(notification);
    }
  }

  private handleChatNotification(notification: Notification): void {
    switch (notification.type) {
      case 'MESSAGE':
      case 'IMAGE':
        this.addMessage(notification);
        break;
      case 'SEEN':
        this.chatMessages.forEach(m => m.state = 'SEEN');
        break;
    }
  }

  private addMessage(notification: Notification): void {
    const message: MessageResponse = this.createMessageResponseByNotification(notification);
    this.chatMessages.push(message);
    this.updateChatLastMessage(notification);
  }

  private createMessageResponseByNotification(notification: Notification): MessageResponse {
    return {
      senderId: notification.senderId,
      receiverId: notification.receiverId,
      content: notification.content,
      type: notification.messageType,
      media: notification.media,
      createdAt: new Date().toISOString()
    };
  }

  private updateChatLastMessage(notification: Notification): void {
    if (notification.type === 'IMAGE') {
      this.selectedChat.lastMessage = 'Attachment';
    } else {
      this.selectedChat.lastMessage = notification.content;
    }
  }

  private handleGeneralNotification(notification: Notification): void {
    const destChat = this.chats.find(c => c.id === notification.chatId);

    if (destChat && notification.type !== 'SEEN') {
      this.updateChat(destChat, notification);
    } else if (notification.type === 'MESSAGE') {
      this.addNewChat(notification);
    }
  }

  private updateChat(destChat: ChatResponse, notification: Notification): void {
    if (notification.type === 'MESSAGE') {
      destChat.lastMessage = notification.content;
    } else if (notification.type === 'IMAGE') {
      destChat.lastMessage = 'Attachment';
    }

    destChat.lastMessageTime = new Date().toISOString();
    destChat.unreadCount! += 1;
  }

  private addNewChat(notification: Notification): void {
    const newChat: ChatResponse = {
      id: notification.chatId,
      senderId: notification.senderId,
      receiverId: notification.receiverId,
      lastMessage: notification.content,
      name: notification.chatName,
      unreadCount: 1,
      lastMessageTime: new Date().toISOString()
    };
    this.chats.unshift(newChat);
  }

  private loadChats() {
    this.chatService.getChatsByReceiver().subscribe({
      next: (res) => this.chats = res,
      error: (err) => console.error('Error loading chats', err)
    });
  }

  private loadMessages(chatId: string) {
    this.messageService.getMessages({'chat-id': chatId}).subscribe({
      next: (messages) => {
        this.chatMessages = messages;
        setTimeout(() => this.scrollToBottom(), 1);
      },
      error: (err) => console.error('Error loading messages', err)
    });
  }

  private createMessageRequest(): MessageRequest {
    return {
      chatId: this.selectedChat.id!,
      senderId: this.getSenderId(),
      receiverId: this.getReceiverId(),
      content: this.messageContent,
      type: 'TEXT'
    };
  }

  private getSenderId(): string {
    if (this.selectedChat.senderId === this.keycloakService.userId) {
      return this.selectedChat.senderId as string;
    }
    return this.selectedChat.receiverId as string;
  }

  private getReceiverId(): string {
    if (this.selectedChat.senderId === this.keycloakService.userId) {
      return this.selectedChat.receiverId as string;
    }
    return this.selectedChat.senderId as string;
  }

  private onMessageSent(): void {
    const message: MessageResponse = this.createMessageResponse('SENT');
    this.chatMessages.push(message);
    this.selectedChat.lastMessage = this.messageContent;
    this.resetMessageContent();
  }

  private createMessageResponse(state: "SENT" | "SEEN" | undefined): MessageResponse {
    return {
      senderId: this.getSenderId(),
      receiverId: this.getReceiverId(),
      content: this.messageContent,
      type: 'TEXT',
      state: state,
      createdAt: new Date().toISOString()
    };
  }

  private resetMessageContent(): void {
    this.messageContent = '';
    this.showEmojis = false;
    setTimeout(() => this.scrollToBottom(), 1);
  }

  private extractFileFromTarget(target: EventTarget | null): File | null {
    const htmlInputTarget = target as HTMLInputElement;
    if (target === null || htmlInputTarget.files === null) {
      return null;
    }
    return htmlInputTarget.files[0];
  }

  private uploadMediaContent(readerResult: any, file: File): void {
    const mediaLines = readerResult?.toString().split(',')[1];

    this.messageService.uploadMedia({
      'chat-id': this.selectedChat.id as string,
      body: { file }
    }).subscribe({
      next: () => this.addUploadedMediaMessage(mediaLines),
    });
  }

  private addUploadedMediaMessage(mediaLines: string): void {
    const message: MessageResponse = {
      senderId: this.getSenderId(),
      receiverId: this.getReceiverId(),
      content: 'Attachment',
      type: 'IMAGE',
      state: 'SENT',
      media: [mediaLines],
      createdAt: new Date().toISOString()
    };
    this.chatMessages.push(message);
  }
}
