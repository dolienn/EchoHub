import {ElementRef, Injectable} from '@angular/core';
import {MessageService} from "./message.service";
import {ChatResponse} from "./models/chat-response";
import {MessageResponse} from "./models/message-response";
import {KeycloakService} from "../../utils/keycloak/keycloak.service";
import {ScrollerService} from "./scroller.service";

@Injectable({
  providedIn: 'root'
})
export class MediaUploaderService {

  selectedChat: ChatResponse = {};
  chatMessages: Array<MessageResponse> = [];
  messageContent: string = '';
  showEmojis: boolean = false;

  constructor(
    private messageService: MessageService,
    private keycloakService: KeycloakService,
    private scrollerService: ScrollerService
  ) { }

  uploadMedia(target: EventTarget | null, messagesRef?: ElementRef): void {
    const file = this.extractFileFromTarget(target);
    if (file) {
      const reader = new FileReader();
      reader.onload = () => this.uploadMediaContent(reader.result, file, messagesRef);
      reader.readAsDataURL(file);
    }
  }

  uploadMediaContent(readerResult: any, file: File, messagesRef?: ElementRef): void {
    const mediaLines = readerResult?.toString().split(',')[1];
    const mediaType = this.getMediaType(file);
    this.messageService.uploadMedia({
      'chat-id': this.selectedChat.id as string,
      body: { file },
      'media-type': mediaType as string
    }).subscribe({
      next: () => {
        this.addUploadedMediaMessage(mediaLines, mediaType);
        this.selectedChat.lastMessage = this.getContentByMediaType(mediaType as "AUDIO" | "IMAGE" | "VIDEO");
        this.resetMessageContent(messagesRef);
      },
      error: (err) => {
        console.error('Error uploading media', err);
        this.resetMessageContent(messagesRef);
      }
    })
  }

  private extractFileFromTarget(target: EventTarget | null): File | null {
    const htmlInputTarget = target as HTMLInputElement;
    if (target === null || htmlInputTarget.files === null) {
      return null;
    }
    return htmlInputTarget.files[0];
  }

  private getMediaType(file: File): "AUDIO" | "IMAGE" | "VIDEO" | undefined {
    const extension = file.name.split('.').pop()?.toLowerCase();

    if (!extension) {
      return undefined;
    }

    switch (extension) {
      case 'mp3':
      case 'wma':
      case 'ogg':
      case 'flac':
      case 'aac':
      case 'wav':
        return 'AUDIO';

      case 'mp4':
      case 'mov':
      case 'mkv':
      case 'webm':
      case 'avi':
      case 'flv':
      case 'wmv':
        return 'VIDEO';

      case 'png':
      case 'jpg':
      case 'jpeg':
      case 'gif':
      case 'bmp':
      case 'svg':
      case 'webp':
        return 'IMAGE';

      default:
        return undefined;
    }
  }

  private addUploadedMediaMessage(mediaLines: string, mediaType: "AUDIO" | "IMAGE" | "VIDEO" | undefined): void {
    const message: MessageResponse = {
      senderId: this.getSenderId(),
      receiverId: this.getReceiverId(),
      content: this.getContentByMediaType(mediaType as "AUDIO" | "IMAGE" | "VIDEO"),
      type: mediaType,
      state: 'SENT',
      media: [mediaLines],
      createdAt: new Date().toISOString()
    };
    this.chatMessages.push(message);
  }

  getSenderId(): string {
    if (this.selectedChat.senderId === this.keycloakService.userId) {
      return this.selectedChat.senderId as string;
    }
    return this.selectedChat.receiverId as string;
  }

  getReceiverId(): string {
    if (this.selectedChat.senderId === this.keycloakService.userId) {
      return this.selectedChat.receiverId as string;
    }
    return this.selectedChat.senderId as string;
  }

  private getContentByMediaType(mediaType: "AUDIO" | "IMAGE" | "VIDEO"): string | undefined {
    switch (mediaType) {
      case 'AUDIO':
        return 'Audio';
      case 'IMAGE':
        return 'Attachment';
      case 'VIDEO':
        return 'Video';
      default:
        return undefined;
    }
  }

  resetMessageContent(messagesRef: ElementRef | undefined): void {
    this.messageContent = '';
    this.showEmojis = false;
    if(messagesRef) {
      setTimeout(() => this.scrollerService.scrollToBottom(messagesRef), 1);
    }
  }
}
