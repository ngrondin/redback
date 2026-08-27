import { AfterViewInit, ElementRef } from "@angular/core";
import { Component } from "@angular/core";
import { Input, OnInit } from "@angular/core";
import { Subscription } from "rxjs";
import { RbActivatorComponent } from "./rb-activator";
import { AppInjector } from "app/app.module";

@Component({template: ''})
export abstract class RbComponent implements OnInit {
    @Input('id') id: string | null = null;
    @Input('activator') activator: RbActivatorComponent | null = null;
    @Input('initialcontrols') initialControls: any = null;

    public pendingControls: any;
    public initiated: boolean = false;
    public active: boolean = false;
    private activatorSubscription: Subscription | null = null;

    constructor() {}

    ngOnInit(): void {
      if(this.activator != null) {
        this.activatorSubscription = this.activator.getActivationObservable().subscribe(state => {
          if(this.active != state) {
            this.active = state;
            this.internalActivationEvent(state);
          }
        });
        this.active = this.activator.activatorOn;
      } else {
        this.active = true;
      }
      this.componentInit();
      this.initiated = true;
      if (this.initialControls != null && this.pendingControls == null) {
        this.control(this.initialControls);
      }
    }

    ngOnDestroy(): void {
        if(this.activatorSubscription != null) {
          this.activatorSubscription.unsubscribe();
        }
        this.componentDestroy();
    }

    internalActivationEvent(state: boolean) {
      this.onActivationEvent(state);
      if(this.active) {
        if(this.pendingControls) {
          this.control(this.pendingControls);
          this.pendingControls = null;
        }
      }
    }

    control(data: any) { // To be used by the view loader or other components to pass in config data
      if(this.initiated && this.active) {
        this.componentControl(data);
      } else {
        this.pendingControls = data;
      }
    }

    abstract componentInit() : void;

    abstract componentDestroy() : void;

    abstract onActivationEvent(state: boolean): void;

    componentControl(data: any) {

    }
}
