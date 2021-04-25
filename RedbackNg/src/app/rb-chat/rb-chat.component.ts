import { Component, OnInit, EventEmitter, Output } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { ClientWSService } from 'app/services/clientws.service';
import { ConfigService } from 'app/services/config.service';
import { DataService } from 'app/services/data.service';

export class Chat {
  id: String;
  object: string;
  uid: string;
  participants: String[];
  messages: ChatMessage[];
  newmessage: String;
  scrollpos: number;
  unread: number;
  linkLabel: String;

  get label() : String {
    return this.id != null ? this.id : this.participants.join(',');
  }

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
  
  @Output() navigate: EventEmitter<any> = new EventEmitter();
  
  opened: boolean;
  dest: String;
  body: String;
  chats: Chat[] = [];
  focusChat: Chat;
  users: String[] = [];

  constructor(
    private apiService: ApiService,
    private clientWSService: ClientWSService,
    private dataService: DataService,
    private configService: ConfigService
  ) { 
    //this.apiService.initChatWebsocket();
    let obs = this.clientWSService.getChatObservable();
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
    if(this.focusChat != null) {
      this.focusChat.unread = 0;
    }
  }

  menuClosed() {
    this.opened = false;
  }

  openChat(chat: Chat) {
    this.focusChat = chat;
    chat.unread = 0;
  }

  keyUp(event: any) {
    let textArea = event.target;   
    textArea.style.overflow = 'hidden';
    textArea.style.height = '0px';
    textArea.style.height = textArea.scrollHeight + 'px';
    if(event.key == "Enter") {
      this.send(this.focusChat);
    }
  }

  receiveMessage(json: any) {
    if(json.type == 'text') {
      let thisChat: Chat = null;
      let from = json.from;
      for(let chat of this.chats) {
        if((json.chatid != null && json.chatid == chat.id) || (json.chatid == null && chat.id == null && chat.participants.indexOf(json.from) > -1)) {
          thisChat = chat;
        }
      }
      if(thisChat == null) {
        thisChat = this.newChatWith(from);
        thisChat.id = json.chatid;
      }
      if(json.object != null && json.uid != null && (json.object != thisChat.object || json.uid != thisChat.uid)) {
        thisChat.object = json.object;
        thisChat.uid = json.uid;
        this.dataService.getServerObject(thisChat.object, thisChat.uid).subscribe((object) => {
          thisChat.linkLabel = this.configService.getLabel(object)
        });
      }
      let msg = new ChatMessage();
      msg.body = json.body;
      msg.date = new Date();
      thisChat.messages.push(msg);
      thisChat.scrollpos = thisChat.scrollpos + 50;
      if(this.focusChat != thisChat || this.opened == false) {
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

  send(chat: Chat) {
    let to: String[] = chat.participants;
    this.clientWSService.sendChat(to, chat.id, chat.object, chat.uid, chat.newmessage);
    let newMsg = new ChatMessage();
    newMsg.body = chat.newmessage;
    newMsg.date = new Date();
    newMsg.fromMe = true;
    chat.messages.push(newMsg);
    chat.newmessage = null;
    chat.scrollpos = chat.scrollpos + 50;
  }

  newChat() {
    this.focusChat = null;
    this.users = [];
    this.clientWSService.pingOtherClients().subscribe(username => this.users.push(username));
  }

  newChatWith(user: String) : Chat {
    this.users = [];
    let chat = new Chat();
    chat.id = null;
    chat.participants = [];
    chat.participants.push(user)
    chat.messages = [];
    chat.scrollpos = 400;
    chat.unread = 0;
    this.chats.push(chat);
    this.focusChat = chat;
    return chat;
  }

  public clickLink(chat: Chat) {
    let evt = {
      object : (chat.object != null ? chat.object : null),
      filter : {
        uid : "'" + chat.uid + "'"
      },
      reset : true
    }
    this.navigate.emit(evt);
  }

  getChat(id: String) : Chat {
    for(let chat of this.chats) {
      if(chat.id == id) {
        return chat;
      }
    }
  }
}
