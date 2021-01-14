import { HostBinding } from '@angular/core';
import { Component, OnInit, HostListener, Output, EventEmitter, Input } from '@angular/core';
import { RbContainerComponent } from 'app/abstract/rb-container';

@Component({
  selector: 'rb-modal',
  exportAs: 'modal',
  templateUrl: './rb-modal.component.html',
  styleUrls: ['./rb-modal.component.css']
})
export class RbModalComponent extends RbContainerComponent {
  @Input('name') name: string;
  @Output('closeModal') closeModal: EventEmitter<any> = new EventEmitter();

  isOpen: boolean = false;
  
  constructor() {
    super();
  }

  containerInit() {
    this.isOpen = false;
  }

  containerDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  @HostListener('click', ['$event']) 
  onMouseMove($event) {
    this.closeModal.emit();
  }

  @HostBinding('style.display') get visitility() {
    return this.isOpen ? 'flex' : 'none';
  }
}
