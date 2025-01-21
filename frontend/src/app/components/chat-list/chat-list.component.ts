import {Component, input, InputSignal, OnInit, output, OutputEmitterRef} from '@angular/core';
import {ChatResponse} from "../../services/models/chat-response";
import {UserResponse} from "../../services/models/user-response";
import {UserService} from "../../services/services/user.service";
import {ChatService} from "../../services/services/chat.service";
import {KeycloakService} from "../../utils/keycloak/keycloak.service";
import {StringResponse} from "../../services/models/string-response";
import {DatePipe, NgIf} from "@angular/common";
import {LoaderComponent} from "../loader/loader.component";
import {LoaderService} from "../../services/services/loader.service";

@Component({
  selector: 'app-chat-list',
  standalone: true,
  imports: [
    DatePipe,
    NgIf,
    LoaderComponent
  ],
  templateUrl: './chat-list.component.html',
  styleUrl: './chat-list.component.scss'
})
export class ChatListComponent implements OnInit {

  chats: InputSignal<ChatResponse[]> = input<ChatResponse[]>([]);
  chatSelected: OutputEmitterRef<ChatResponse> = output<ChatResponse>();

  isChatsLoading: boolean = true;
  filteredChats: ChatResponse[] = [];
  searchNewContact: boolean = false;
  contacts: UserResponse[] = [];
  filteredContacts: UserResponse[] = [];
  filterType: 'all' | 'unread' | 'favorites' | 'search' = 'all';
  searchQuery: string = '';

  constructor(
    private chatService: ChatService,
    private userService: UserService,
    private keycloakService: KeycloakService,
    private loaderService: LoaderService
  ) {
  }

  ngOnInit() {
    this.updateFilteredContacts();
    this.subscribeToLoadingState();
  }

  searchContact(): void {
    this.userService.getAllUsers().subscribe({
      next: (users: UserResponse[]) => {
        this.contacts = users;
        this.updateFilteredContacts();
        this.searchNewContact = true;
      },
      error: (err) => {
        console.error('Error fetching users:', err);
      },
    });
  }

  selectContact(contact: UserResponse): void {
    const senderId = this.keycloakService.userId as string;

    this.chatService.createChat({
      'sender-id': senderId,
      'receiver-id': contact.id as string,
    }).subscribe({
      next: (res: StringResponse) => {
        this.handleNewChat(res, contact, senderId);
      },
      error: (err) => {
        console.error('Error creating chat:', err);
      },
    });
  }

  changeFilterType(type: 'all' | 'unread' | 'favorites' | 'search'): void {
    this.filteredChats = [];
    this.filterType = type;

    if (type === 'unread') {
      this.filteredChats = this.getUnreadChats();
    }
  }

  onSearchChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery = input.value.toLowerCase();

    if (this.searchQuery) {
      this.changeFilterType('search');
      this.filteredChats = this.filterChatsByQuery(this.searchQuery);
    }
  }

  chatClicked(chat: ChatResponse) {
    this.chatSelected.emit(chat);
  }

  wrapMessage(message: string | undefined): string {
    return message && message.length > 20 ? `${message.slice(0, 17)}...` : message || '';
  }

  private updateFilteredContacts(): void {
    const chatUserIds = this.getChatUserIds();
    this.filteredContacts = this.contacts.filter(contact => !chatUserIds.has(contact.id as string));
  }

  private subscribeToLoadingState(): void {
    this.loaderService.isChatsLoading$.subscribe((isLoading: boolean) => {
      this.isChatsLoading = isLoading;
    });
  }

  private getChatUserIds(): Set<string> {
    return new Set(
      this.chats().flatMap(chat => [chat.senderId, chat.receiverId])
    ) as Set<string>;
  }

  private handleNewChat(res: StringResponse, contact: UserResponse, senderId: string): void {
    const newChat: ChatResponse = {
      id: res.response,
      name: `${contact.firstName} ${contact.lastName}`,
      recipientOnline: contact.online,
      lastMessageTime: contact.lastSeen,
      senderId,
      receiverId: contact.id,
    };

    this.chats().unshift(newChat);
    this.searchNewContact = false;
    this.chatSelected.emit(newChat);
    this.updateFilteredContacts();
  }

  private getUnreadChats(): ChatResponse[] {
    return this.chats().filter(chat => chat.unreadCount && chat.unreadCount > 0);
  }

  private filterChatsByQuery(query: string): ChatResponse[] {
    return this.chats().filter(chat => chat.name?.toLowerCase().includes(query));
  }
}
