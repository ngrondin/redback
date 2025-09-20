import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Chat, ChatMessage, ChatUser } from 'app/datamodel';
import { Observable } from 'rxjs';
import { ClientWSService } from './clientws.service';



@Injectable({
  providedIn: 'root'
})
export class ChatService {
  chatUsers: ChatUser[];
  chats: Chat[];
  autoReadForChat: Chat = null;
  unreadCount: number = 0;

  constructor(
    private apiService: ApiService,
    private clientService: ClientWSService
  ) { 
    clientService.getChatObservable().subscribe(msg => this.receiveUpdate(msg));
  }

  load() : Promise<void> {
    return new Promise<void>((resolve, reject) => {
      if(this.apiService.chatService != null && this.apiService.chatService != "") {
        this.loadUsers().subscribe(data => {
          this.loadChats().subscribe(data => {
            this.loadAllUnreadChatMessages().subscribe(() => {
              resolve();
            });
          });
        })
      } else {
        resolve();
      }
    });
  }

  loadUsers(): Observable<ChatUser[]> {
    return new Observable<ChatUser[]>((observer) => {
      this.apiService.listChatUsers().subscribe(resp => {
        this.chatUsers = [];
        if(resp.list != null) 
          for(var json of resp.list) 
            this.processChatUserJson(json);
        observer.next(this.chatUsers);
        observer.complete();
      })
    });
  }

  loadChats(): Observable<Chat[]> {
    return new Observable<Chat[]>((observer) => {
      this.apiService.listChats().subscribe(resp => {
        this.chats = [];
        if(resp.list != null) 
          for(var json of resp.list) 
            this.processChatJson(json);
        observer.next(this.chats);
        observer.complete();
      });
    });
  }

  loadChatMessages(chat: Chat): Observable<ChatMessage[]>  {
    return new Observable<ChatMessage[]>((observer) => {
      this.apiService.listChatMessages(chat.id).subscribe(resp => {
        chat.messages = [];
        if(resp.list != null) 
          for(var json of resp.list) 
            this.processChatMessageJson(chat, json);
        this.calcStats();    
        observer.next(chat.messages);
        observer.complete();
      });
    });
  }

  loadAllUnreadChatMessages(): Observable<null>  {
    return new Observable<null>((observer) => {
      this.apiService.listAllUnreadChatMessages().subscribe(resp => {
        if(resp.list != null) {
          for(var json of resp.list) {
            let chat: Chat = this.chats.find(c => c.id == json.conversation);
            if(chat != null)
              this.processChatMessageJson(chat, json);
          }
        }
        this.calcStats();    
        observer.next(null);
        observer.complete();
      });
    });
  }

  createChat(name: string): Observable<Chat> {
    return new Observable<Chat>((observer) => {
      this.apiService.createChat(name).subscribe(resp => {
        observer.next(this.processChatJson(resp));
        observer.complete();
      });
    });  
  }

  addUser(chat: Chat, user: ChatUser): Observable<Chat>  {
    return new Observable<Chat>((observer) => {
      this.apiService.addUserToChat(chat.id, user.username).subscribe(resp => {
        observer.next(this.processChatJson(resp));
        observer.complete();
      });
    });
  }

  removeUser(chat: Chat, user: ChatUser): Observable<Chat>  {
    return new Observable<Chat>((observer) => {
      this.apiService.removeUserFromChat(chat.id, user.username).subscribe(resp => {
        observer.next(this.processChatJson(resp));
        observer.complete();
      });
    });
  }

  sendMessage(chat: Chat, body: string): Observable<ChatMessage> {
    return new Observable<ChatMessage>((observer) => {
      this.apiService.sendChatMessage(chat.id, body).subscribe(resp => {
        observer.next(this.processChatMessageJson(chat, resp));
        observer.complete();
      });
    });
  }

  private receiveUpdate(msg: any) {
    if(msg.type == 'conversation') {
      this.processChatJson(msg);
    } else if(msg.type == 'message') {
      let chat: Chat = this.chats.find(c => c.id == msg.conversation);
      this.processChatMessageJson(chat, msg);
    }
    this.calcStats();            
  }

  private processChatUserJson(json) : ChatUser {
    let user = this.chatUsers.find(u => u.id == json.id);
    if(user == null) {
      user = new ChatUser(json);
      this.chatUsers.push(user);
    } else {
      user.available = json.available;
    }
    return user;
  }

  private processChatJson(json) : Chat {
    let chat = this.chats.find(c => c.id == json.id);
    if(chat == null) {
      chat = new Chat(json);
      this.chats.push(chat);
    } else {
      chat.latest = new Date(json.latest);
    }
    this.chats.sort((a, b) => a.latest.getTime() - b.latest.getTime());
    chat.users = this.chatUsers.filter(u => json.users.indexOf(u.username) > -1);
    return chat;
  }

  private processChatMessageJson(chat: Chat, json) : ChatMessage {
    let msg = chat.messages.find(m => m.id == json.id);
    if(msg == null) {
      msg = new ChatMessage(json);
      msg.from = this.findOrCreateUser(json.from);
      chat.messages.push(msg);
    }
    chat.messages.sort((a, b) => a.date.getTime() - b.date.getTime());
    if(json.readby != null)
      msg.readby = this.chatUsers.filter(u => json.readby.indexOf(u.username) > -1);
    if(!msg.isRead) {
      if(this.autoReadForChat == chat) {
        msg.readby = msg.readby.concat(this.chatUsers.filter(u => u.username == window.redback.username));
        this.apiService.markChatMessageAsRead(msg.id).subscribe(resp => { }); 
      }
    }
    return msg;
  }

  private calcStats() {
    this.unreadCount = 0;
    for(var chat of this.chats) {
      chat.unreadCount = 0;
      for(var msg of chat.messages) {
        if(!msg.isRead) {
          this.unreadCount++;
          chat.unreadCount++;
        }
      }
    }
  }

  private findOrCreateUser(username: string) {
    let user = this.chatUsers.find(u => u.username == username);
    if(user == null) {
      user = new ChatUser({username: username, fullname: "Unknown User"});
      this.chatUsers.push(user);
    }
    return user;
  }
}
