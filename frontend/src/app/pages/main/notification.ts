export interface Notification {
  chatId?: string;
  content?: string;
  senderId?: string;
  receiverId?: string;
  messageType?: 'TEXT' | 'IMAGE' | 'AUDIO' | 'VIDEO';
  type?: 'MESSAGE' | 'SEEN' | 'IMAGE' | 'AUDIO' | 'VIDEO';
  chatName?: string;
  media?: Array<string>;
}
