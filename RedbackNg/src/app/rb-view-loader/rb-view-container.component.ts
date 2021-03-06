import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange } from '@angular/core';
import { ViewTarget } from 'app/datamodel';

@Component({
    selector: 'rb-view-container',
    template: '<div>View Container</div>'
  })
  export class ViewContainerComponent {
    @Input('target') currentTarget: ViewTarget;
    @Output() navigate: EventEmitter<any> = new EventEmitter();
    
    activeModal: string;

    navigateTo(target: any) {
      this.navigate.emit(target);
    }

    openModal(name: string) {
      this.activeModal = name;
    }

    closeModal() {
      this.activeModal = null;
    }

    setTitle(title: string) {
      setTimeout(() => this.currentTarget.title = title);
    }

  };