import { Injectable } from '@angular/core';
import { RbModalComponent } from 'app/rb-modal/rb-modal.component';
import { LoadedView } from 'app/rb-view-loader/rb-view-loader-model';

@Injectable({
  providedIn: 'root'
})
export class ModalService {
  currentView: string = null;
  modals: {[view:string]: {[name:string]: RbModalComponent}} = {};

  constructor() { }

  public register(name: string, view: string,  modal: RbModalComponent) {
    if(this.modals[view] == null) this.modals[view] = {};
    this.modals[view][name] = modal;
  }

  public setCurrentView(view: string) {
    this.currentView = view;
    console.log("Current view is " + view);
  }

  public open(name: string, view: string = this.currentView) {
    let currentViewModals = this.modals[view];
    if(currentViewModals != null &&  currentViewModals[name] != null && !currentViewModals[name].isOpen) {
      currentViewModals[name].open();
      console.log("called open " + name + " in " + view);
    }
  }

  public close(name: string, view: string = this.currentView) {
    let currentViewModals = this.modals[view];
    if(currentViewModals != null && currentViewModals[name] != null && currentViewModals[name].isOpen) {
      currentViewModals[name].close();
      console.log("called close " + name + " in " + view);
    }
  }

  public closeAll() {
    for(let view in this.modals) {
      for(let name in this.modals[view]) {
        this.close(name, view);
      }  
    }
  }

}
