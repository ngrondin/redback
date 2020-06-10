import { Component, OnInit } from '@angular/core';
import { ApiService } from 'app/api.service';

export class Chat {
  id: String;
  messages: ChatMessage[];
  newmessage: String;
  scrollpos: number;
  unread: number;
}

export class ChatMessage {
  date: Date;
  body: String;
  fromMe: boolean;
}

@Component({
  selector: 'rb-chat',
  templateUrl: './rb-chat.component.html',
  styleUrls: ['./rb-chat.component.css']
})
export class RbChatComponent implements OnInit {
  opened: boolean;
  dest: String;
  body: String;
  chats: Chat[] = [];
  openId: String;
  users: String[] = [];

  constructor(
    private apiService: ApiService
  ) { 
    this.apiService.initChatWebsocket();
    let obs = this.apiService.getChatObservable();
    if(obs != null) {
      obs.subscribe(json => this.receiveMessage(json));
    }
    this.opened = false;
  }

  ngOnInit(): void {
  }

  get totalUnread() {
    let c = 0;
    for(let chat of this.chats) {
      c = c + chat.unread;
    }
    return c;
  }

  menuOpened() {
    this.opened = true;
    if(this.openId != null) {
      this.getChat(this.openId).unread = 0;
    }
  }

  menuClosed() {
    this.opened = false;
  }

  openChat(id: String) {
    this.openId = id;
    this.getChat(this.openId).unread = 0;
  }

  keyUp(event: any) {
    let textArea = event.target;   
    textArea.style.overflow = 'hidden';
    textArea.style.height = '0px';
    textArea.style.height = textArea.scrollHeight + 'px';
    if(event.key == "Enter") {
      this.send(this.openId);
    }
  }

  receiveMessage(json: any) {
    if(json.type == 'text') {
      let thisChat: Chat = null;
      let from = json.from;
      for(let chat of this.chats) {
        if(chat.id == from) {
          thisChat = chat;
        }
      }
      if(thisChat == null) {
        thisChat = this.newChatWith(from);
      }
      let msg = new ChatMessage();
      msg.body = json.body;
      msg.date = new Date();
      thisChat.messages.push(msg);
      thisChat.scrollpos = thisChat.scrollpos + 50;
      if(this.openId != thisChat.id || this.opened == false) {
        thisChat.unread = thisChat.unread + 1;
      }

    } else if(json.type == 'users') {
      let users = json.users;
      for(let user of users) {
        let exists = false;
        for(let chat of this.chats) {
          if(chat.id == user) {
            exists = true;
          }
        }
        if(!exists) {
          this.users.push(user);
        }
      }
    }

  }

  send(id: String) {
    for(let chat of this.chats) {
      if(id == chat.id) {
        let to: String[] = [];
        to.push(chat.id);
        this.apiService.sendChat(to, chat.newmessage);
        let newMsg = new ChatMessage();
        newMsg.body = chat.newmessage;
        newMsg.date = new Date();
        newMsg.fromMe = true;
        chat.messages.push(newMsg);
        chat.newmessage = null;
        chat.scrollpos = chat.scrollpos + 50;
      }
    }
  }

  newChat() {
    this.openId = null;
    this.apiService.getChatUsers();
  }

  newChatWith(user: String) : Chat {
    this.users = [];
    let chat = new Chat();
    chat.id = user;
    chat.messages = [];
    chat.scrollpos = 400;
    chat.unread = 0;
    this.chats.push(chat);
    this.openId = chat.id;
    return chat;
  }

  getChat(id: String) : Chat {
    for(let chat of this.chats) {
      if(chat.id == id) {
        return chat;
      }
    }
  }
}
