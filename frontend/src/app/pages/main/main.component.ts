import {Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Notification} from "./notification";
import {ChatResponse} from "../../services/models/chat-response";
import {ChatService} from "../../services/services/chat.service";
import {KeycloakService} from "../../utils/keycloak/keycloak.service";
import {MessageResponse} from "../../services/models/message-response";
import {MessageRequest} from "../../services/models/message-request";
import {MessageService} from "../../services/services/message.service";
import {PickerComponent} from "@ctrl/ngx-emoji-mart";
import {DatePipe, NgClass, NgIf} from "@angular/common";
import {ChatListComponent} from "../../components/chat-list/chat-list.component";
import {FormsModule} from "@angular/forms";
import {EmojiData} from "@ctrl/ngx-emoji-mart/ngx-emoji";
import {LoaderComponent} from "../../components/loader/loader.component";
import {WebSocketService} from "../../services/services/websocket.service";
import {LoaderService} from "../../services/services/loader.service";
import {AudioRecorderService} from "../../services/services/audio-recorder.service";
import {MediaUploaderService} from "../../services/services/media-uploader.service";
import {ScrollerService} from "../../services/services/scroller.service";

@Component({
  selector: 'app-main',
  standalone: true,
  imports: [
    PickerComponent,
    DatePipe,
    ChatListComponent,
    FormsModule,
    LoaderComponent,
    NgIf,
    NgClass
  ],
  templateUrl: './main.component.html',
  styleUrl: './main.component.scss'
})
export class MainComponent implements OnInit, OnDestroy {
  @ViewChild('messages') messagesRef!: ElementRef;

  private notificationSubscription: any;

  chats: Array<ChatResponse> = [];
  isMessagesLoading: boolean = true;

  constructor(
    private chatService: ChatService,
    private messageService: MessageService,
    private keycloakService: KeycloakService,
    private webSocketService: WebSocketService,
    private loaderService: LoaderService,
    protected audioRecorderService: AudioRecorderService,
    protected mediaUploaderService: MediaUploaderService,
    private scrollerService: ScrollerService
  ) {
  }

  ngOnInit(): void {
    this.initWebSocket();
    this.loadChats();
    setTimeout(() => this.scrollerService.scrollToBottom(this.messagesRef), 1);
  }

  ngOnDestroy(): void {
    this.webSocketService.disconnect();
    this.notificationSubscription.unsubscribe();
  }

  chatSelected(chatResponse: ChatResponse): void {
    this.mediaUploaderService.selectedChat = chatResponse;
    this.loadMessages(chatResponse.id!);
    this.markMessagesAsSeen();
  }

  markMessagesAsSeen(): void {
    if (this.mediaUploaderService.selectedChat.id) {
      this.messageService.setMessagesToSeen({ 'chat-id': this.mediaUploaderService.selectedChat.id }).subscribe();
      this.mediaUploaderService.selectedChat.unreadCount = 0;
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
    if (this.mediaUploaderService.messageContent.trim()) {
      const messageRequest: MessageRequest = this.createMessageRequest();
      this.messageService.saveMessage({ body: messageRequest }).subscribe({
        next: () => this.onMessageSent(),
        error: (err) => console.error('Error sending message', err)
      });
    }
  }

  onSelectEmojis(emojiSelected: any) {
    const emoji: EmojiData = emojiSelected.emoji;
    this.mediaUploaderService.messageContent += emoji.native;
  }

  uploadMedia(target: EventTarget | null): void {
    this.mediaUploaderService.uploadMedia(target);
  }

  logout(): void {
    this.keycloakService.logout();
  }

  userProfile(): void {
    this.keycloakService.accountManagement();
  }

  toggleRecording(): void {
    this.audioRecorderService.toggleRecording();
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
    if (this.mediaUploaderService.selectedChat && this.mediaUploaderService.selectedChat.id === notification.chatId) {
      this.handleChatNotification(notification);
    } else {
      this.handleGeneralNotification(notification);
    }
  }

  private handleChatNotification(notification: Notification): void {
    switch (notification.type) {
      case 'MESSAGE':
      case 'IMAGE':
      case 'AUDIO':
      case 'VIDEO':
        this.addMessage(notification);
        break;
      case 'SEEN':
        this.mediaUploaderService.chatMessages.forEach(m => m.state = 'SEEN');
        break;
    }
  }

  private addMessage(notification: Notification): void {
    const message: MessageResponse = this.createMessageResponseByNotification(notification);
    this.mediaUploaderService.chatMessages.push(message);
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
    console.log(notification.type);
    if (notification.type === 'IMAGE') {
      this.mediaUploaderService.selectedChat.lastMessage = 'Attachment';
    } else if (notification.type === 'AUDIO') {
      this.mediaUploaderService.selectedChat.lastMessage = 'Audio';
    } else if (notification.type === 'VIDEO') {
      this.mediaUploaderService.selectedChat.lastMessage = 'Video';
    } else {
      this.mediaUploaderService.selectedChat.lastMessage = notification.content;
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
    } else if (notification.type === 'AUDIO') {
      destChat.lastMessage = 'Audio';
    } else if (notification.type === 'VIDEO') {
      destChat.lastMessage = 'Video';
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
    this.loaderService.setLoadingState(true);
    this.chatService.getChatsByReceiver().subscribe({
      next: (res) => {
        this.chats = res
        this.loaderService.setLoadingState(false);
      },
      error: (err) => {
        this.loaderService.setLoadingState(false);
        console.error('Error loading chats', err);
      }
    });
  }

  private loadMessages(chatId: string) {
    this.isMessagesLoading = true;
    this.messageService.getMessages({'chat-id': chatId}).subscribe({
      next: (messages) => {
        this.mediaUploaderService.chatMessages = messages;
        this.isMessagesLoading = false;
        setTimeout(() => this.scrollerService.scrollToBottom(this.messagesRef), 1);
      },
      error: (err) => {
        this.isMessagesLoading = false;
        console.error('Error loading messages', err)
      }
    });
  }

  private createMessageRequest(): MessageRequest {
    return {
      chatId: this.mediaUploaderService.selectedChat.id!,
      senderId: this.mediaUploaderService.getSenderId(),
      receiverId: this.mediaUploaderService.getReceiverId(),
      content: this.mediaUploaderService.messageContent,
      type: 'TEXT'
    };
  }

  private onMessageSent(): void {
    const message: MessageResponse = this.createMessageResponse('SENT');
    this.mediaUploaderService.chatMessages.push(message);
    this.mediaUploaderService.selectedChat.lastMessage = this.mediaUploaderService.messageContent;
    this.mediaUploaderService.resetMessageContent(this.messagesRef);
  }

  private createMessageResponse(state: "SENT" | "SEEN" | undefined): MessageResponse {
    return {
      senderId: this.mediaUploaderService.getSenderId(),
      receiverId: this.mediaUploaderService.getReceiverId(),
      content: this.mediaUploaderService.messageContent,
      type: 'TEXT',
      state: state,
      createdAt: new Date().toISOString()
    };
  }
}
