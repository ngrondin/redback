import { AfterViewInit, ElementRef } from "@angular/core";
import { Component } from "@angular/core";
import { Input, OnInit } from "@angular/core";
import { Subscription } from "rxjs";
import { RbActivatorComponent } from "./rb-activator";
import { AppInjector } from "app/app.module";

@Component({template: ''})
export abstract class RbComponent implements OnInit {
    @Input('id') id: string;
    @Input('activator') activator: RbActivatorComponent;

    public initiated: boolean = false;
    public active: boolean = false;
    private activatorSubscription: Subscription;

    constructor() {}
  
    ngOnInit(): void {
      if(this.activator != null) {
        this.activatorSubscription = this.activator.getActivationObservable().subscribe(state => {
          this.active = state;
          this.onActivationEvent(state);
        });
        this.active = this.activator.activatorOn;
      } else {
        this.active = true;
      }
      this.componentInit();
      this.initiated = true;
      this.onActivationEvent(this.active);
    }

    ngOnDestroy(): void {
        if(this.activatorSubscription != null) {
          this.activatorSubscription.unsubscribe();
        }
        this.componentDestroy();
    }

    /*get active() : boolean {
      return this.activator != null ? this.activator.activatorOn : true;
    }*/

    abstract componentInit();

    abstract componentDestroy();

    abstract onActivationEvent(state: boolean);
}