import { Injectable } from '@angular/core';
import { ClientWSService } from 'app/services/clientws.service';
import { InitialsMaker } from 'app/helpers';
import { UserprefService } from './userpref.service';
import { NavigateService } from './navigate.service';
import { interval, Subscription } from 'rxjs';

export class OtherUser {
  username: string;
  fullname?: string;
  initials?: string;
  currentview?: string;
  lastupdate: Date;

  constructor(un: string) {
    this.username = un;
  }

  setFullName(fn: string) {
    this.fullname = fn;
    this.initials = InitialsMaker.createInitials(this.fullname)
  }
}

@Injectable({
  providedIn: 'root'
})
export class CollabService {
  otherUsers: OtherUser[] = [];
  intervalSub?: Subscription;

  constructor(
    private clientService: ClientWSService,
    private userprefService: UserprefService,
    private navigateService: NavigateService
  ) {
    this.clientService.getStateObservable().subscribe(state => this.onClientState(state));
    this.clientService.getUserUpdateObservable().subscribe(update => this.onUserUpdate(update));
    this.navigateService.getNavigateObservable().subscribe(navdata => this.sendCurrentPage(navdata.view));
  }

  onClientState(state: any) {
    if (state.connected == true) {
      this.sendMainUpdate();
      this.intervalSub = interval(30000).subscribe(() => this.sendMainUpdate());
    } else {
      this.intervalSub?.unsubscribe();
    }
  }

  onUserUpdate(update: any) {
    let otherUser = this.otherUsers.find(ou => ou.username == update.username);
    if (update.connected == false) {
      if(otherUser != null) {
        this.otherUsers = this.otherUsers.filter(ou => ou.username != update.username)
      }
    } else {
      if (otherUser == null) {
        otherUser = new OtherUser(update.username);
        this.otherUsers.push(otherUser);
      }
      if (update.fullname != null) otherUser.setFullName(update.fullname);
      if (update.view != null) otherUser.currentview = update.view;
      otherUser.lastupdate = new Date();
    }
  }

  getOtherUsersOnView(viewname: string): OtherUser[] {
    return this.otherUsers.filter(ou => ou.currentview == viewname);
  }

  sendMainUpdate() {
    this.clientService.sendUserUpdate({ connected: true, fullname: this.userprefService.userdisplay });
  }

  sendCurrentPage(viewname: string) {
    this.clientService.sendUserUpdate({ view: viewname });
  }

}
