import { HostBinding } from '@angular/core';
import { Component, OnInit, HostListener, Output, EventEmitter, Input } from '@angular/core';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbContainerComponent } from 'app/abstract/rb-container';
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
  @HostBinding('style.display') get visitility() {return this.isOpen ? 'flex' : 'none'; }
  @HostListener('click', ['$event']) onMouseMove($event) {this.close() }

  isOpen: boolean = false;
  
  constructor(
    private modalService: ModalService
  ) {
    super();
  }

  activatorInit() {
    this.isOpen = false;
    this.modalService.register(this.name, this);
  }

  activatorDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  public open() {
    this.isOpen = true;
    this.activate();
  }

  public close() {
    this.isOpen = false;
    this.deactivate();
  }


}
