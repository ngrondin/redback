import { AfterViewInit } from "@angular/core";
import { Component } from "@angular/core";
import { Input, OnInit } from "@angular/core";
import { Subscription } from "rxjs";
import { RbActivatorComponent } from "./rb-activator";

@Component({template: ''})
export abstract class RbComponent implements OnInit/*, AfterViewInit*/ {
    @Input('id') id: string;
    @Input('activator') activator: RbActivatorComponent;

    public initiated: boolean = false;
    //public active: boolean;
    private activatorSubscription: Subscription;
    //private start: number;

    constructor() { 
      //this.start = (new Date()).getTime();
    }
  
    ngOnInit(): void {
      if(this.activator != null) {
        //this.active = this.activator.activatorOn;
        this.activatorSubscription = this.activator.getActivationObservable().subscribe(state => this.onActivationEvent(state));
      } else {
        //this.active = true;
      }
      this.componentInit();
      this.initiated = true;
    }

    /*ngAfterViewInit(): void {
      let end: number = (new Date()).getTime();
      console.log(this.constructor.name + ' ' + end + ' ' + (end - this.start));
    }*/
    
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

    /*setActive(state: boolean) {
        this.active = state;
        this.onActivationEvent(state);
    }*/
}