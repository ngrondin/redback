import { Injectable } from '@angular/core';
import { RbModalComponent } from 'app/rb-modal/rb-modal.component';

@Injectable({
  providedIn: 'root'
})
export class ModalService {

  modals: {[key:string]: RbModalComponent} = {};

  constructor() { }

  public register(name: string, modal: RbModalComponent) {
    this.modals[name] = modal;
  }

  public open(name: string) {
    if(this.modals[name] != null && !this.modals[name].isOpen) {
      this.modals[name].open();
    }
  }

  public close(name: string) {
    if(this.modals[name] != null && this.modals[name].isOpen) {
      this.modals[name].close();
    }
  }

  public closeAll() {
    for(let name in this.modals) {
      this.close(name);
    }
  }

}
