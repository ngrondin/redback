import { HostBinding } from '@angular/core';
import { Component, OnInit, HostListener, Output, EventEmitter, Input } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { ModalService } from 'app/services/modal.service';

@Component({
  selector: 'rb-modal',
  exportAs: 'modal',
  templateUrl: './rb-modal.component.html',
  styleUrls: ['./rb-modal.component.css']
})
export class RbModalComponent extends RbContainerComponent {
  @Input('name') name: string;
  @HostBinding('style.display') get visitility() {return this.isOpen ? 'flex' : 'none'; }
  @HostListener('click', ['$event']) onMouseMove($event) {this.close() }

  isOpen: boolean = false;
  
  constructor(
    private modalService: ModalService
  ) {
    super();
  }

  containerInit() {
    this.isOpen = false;
    this.modalService.register(this.name, this);
  }

  containerDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  public open() {
    this.isOpen = true;
  }

  public close() {
    this.isOpen = false;
  }


}
