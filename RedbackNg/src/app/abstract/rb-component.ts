import { AfterViewInit } from "@angular/core";
import { Component } from "@angular/core";
import { Input, OnInit } from "@angular/core";
import { Subscription } from "rxjs";
import { RbActivatorComponent } from "./rb-activator";

@Component({template: ''})
export abstract class RbComponent implements OnInit {
    @Input('id') id: string;
    @Input('activator') activator: RbActivatorComponent;

    public initiated: boolean = false;
    private activatorSubscription: Subscription;

    constructor() { 
    }
  
    ngOnInit(): void {
      if(this.activator != null) {
        this.activatorSubscription = this.activator.getActivationObservable().subscribe(state => this.onActivationEvent(state));
      } else {
      }
      this.componentInit();
      this.initiated = true;
    }

    ngOnDestroy(): void {
        if(this.activatorSubscription != null) {
          this.activatorSubscription.unsubscribe();
        }
        this.componentDestroy();
    }

    get active() : boolean {
      return this.activator != null ? this.activator.activatorOn : true;
    }

    abstract componentInit();

    abstract componentDestroy();

    abstract onActivationEvent(state: boolean);
}