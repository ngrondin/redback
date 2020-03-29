import { Component, ViewChild, ViewContainerRef, ComponentRef, Compiler, ComponentFactory, NgModule, ModuleWithComponentFactories, ComponentFactoryResolver, OnInit, Input, Output, EventEmitter, SimpleChange } from '@angular/core';
import { Target } from 'app/desktop-root/desktop-root.component';

@Component({
    selector: 'rb-view-container',
    template: '<div>View Container</div>'
  })
  export class ViewContainerComponent {
    @Input('target') currentTarget: Target;
    @Output() navigate: EventEmitter<any> = new EventEmitter();
    //@Output() titlechange: EventEmitter<any> = new EventEmitter();

    navigateTo(target: any) {
      this.navigate.emit(target);
    }
    setTitle(title: string) {
      this.currentTarget.title = title;
      //this.titlechange.emit(title);
    }

  };