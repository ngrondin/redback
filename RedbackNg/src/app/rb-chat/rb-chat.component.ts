import { Component, OnInit, EventEmitter, Output, ElementRef, ViewChild } from '@angular/core';
import { Chat, ChatUser } from 'app/datamodel';
import { Formatter } from 'app/helpers';
import { ApiService } from 'app/services/api.service';
import { ChatService } from 'app/services/chat.service';
import { ClientWSService } from 'app/services/clientws.service';
import { ConfigService } from 'app/services/config.service';
import { DataService } from 'app/services/data.service';



@Component({
  selector: 'rb-chat',
  templateUrl: './rb-chat.component.html',
  styleUrls: ['./rb-chat.component.css']
})
export class RbChatComponent implements OnInit {
  @Output() navigate: EventEmitter<any> = new EventEmitter();
  @Output() close: EventEmitter<any> = new EventEmitter();

  @ViewChild('createinput') createinput: ElementRef;
  @ViewChild('messagescroll') messagescroll: ElementRef;

  selectedChat: Chat = null;
  creatingChat: boolean = false;
  conversationMode: string = 'messages';
  textToSend: string;
  newChatName: string;

  constructor(
    private chatService: ChatService
  ) { 

  }

  ngOnInit(): void {
    this.chatService.load();
  }

  get chats(): Chat[] {
    return this.chatService.chats;
  }

  get username(): string {
    return window.redback.username;
  }

  public selectChat(chat: Chat) {
    this.chatService.autoReadForChat = chat;
    this.chatService.loadChatMessages(chat).subscribe(list => {
      this.selectedChat = chat;
      this.scrollToBottom();
    })
  }

  public unselectChat() {
    this.selectedChat = null;
    this.chatService.autoReadForChat = null;
  }

  public closeChat() {
    this.close.emit();
  }

  public changeConversationMode(mode) {
    this.conversationMode = mode;
  }

  public createChat() {
    this.selectedChat = null;
    this.creatingChat = true;
    setTimeout(()=>{
      this.createinput.nativeElement.focus();
    },0); 
  }

  public chatCreationKeydown(event) {
    if(event.keyCode == 13) {
      this.chatService.createChat(this.newChatName).subscribe(chat => {
        this.closeCreation();
      });
      console.log(this.newChatName);
    } else if(event.keyCode == 27) {
      this.closeCreation();
    }
  }

  public chatCreationBlur() {
    if(this.creatingChat) {
      this.closeCreation();
    }
  }

  private closeCreation() {
    this.creatingChat = false;
    this.newChatName = null;
  }

  public addUser(user: ChatUser) {
    this.chatService.addUser(this.selectedChat, user).subscribe(chat => { });
  }

  public removeUser(user: ChatUser) {
    this.chatService.removeUser(this.selectedChat, user).subscribe(chat => { });
  }

  public chatInputKeydown(event) {
    if(event.keyCode == 13) {
      this.post();
    }
  }

  public post() {
    this.chatService.sendMessage(this.selectedChat, this.textToSend).subscribe(msg => {
      this.textToSend = null;
      this.scrollToBottom();
    })
  }

  public formatDate(date: Date) : string {
    return date != null && date.getTime != null ? Formatter.formatDateTime(date) : "";
  }

  public listOtherUsers(currentUsers: ChatUser[]) : ChatUser[] {
    let currentUsernames = currentUsers.map(u => u.username);
    let users = [];
    for(var user of this.chatService.chatUsers) 
      if(currentUsernames.indexOf(user.username) == -1)
        users.push(user);
    return users.sort((a, b) => a.fullname.localeCompare(b.fullname));
  }

  private scrollToBottom() {
    setTimeout(() => {
      if(this.messagescroll != null) {
        this.messagescroll.nativeElement.scrollTop = this.messagescroll.nativeElement.scrollHeight;
      }
    }, 100);
  }
}


