import { HostBinding } from '@angular/core';
import { Component, OnInit, HostListener, Output, EventEmitter, Input } from '@angular/core';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { LogService } from 'app/services/log.service';
import { ModalService } from 'app/services/modal.service';

@Component({
  selector: 'rb-modal',
  exportAs: 'modal',
  templateUrl: './rb-modal.component.html',
  styleUrls: ['./rb-modal.component.css']
})
export class RbModalComponent extends RbActivatorComponent {

  @Input('name') name: string;
  @Input('title') title: string;
  @Input('icon') icon: string;
  @Input('eventscript') _eventscript: string;
  @HostBinding('style.display') get visitility() {return this.isOpen ? 'flex' : 'none'; }
  @HostListener('click', ['$event']) backgroundClick($event) {this.close() }

  isOpen: boolean = false;
  private eventScript: Function;
  
  constructor(
    private modalService: ModalService,
    private logService: LogService,
  ) {
    super();
  }

  activatorInit() {
    this.isOpen = false;
    if(this._eventscript != null) {
      this.eventScript = Function("event", this._eventscript);
    }    
  }

  activatorDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(state: boolean) {
  }

  public open() {
    this.isOpen = true;
    this.activate();
    this.runEventScript("open");
  }

  public close() {
    this.isOpen = false;
    this.deactivate();
    this.runEventScript("close");
  }

  private runEventScript(event: string) {
    if(this.eventScript != null) {
      try {
        this.eventScript.call(window.redback, {event: event});
      } catch(err) {
        this.logService.error(err);
      }
    }
  }

}
