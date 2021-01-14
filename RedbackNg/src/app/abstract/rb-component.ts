import { Input, OnInit } from "@angular/core";
import { Subscription } from "rxjs";
import { RbActivatorComponent } from "./rb-activator";


export abstract class RbComponent implements OnInit {
    @Input('activator') activator: RbActivatorComponent;

    public active: boolean;
    private activatorSubscription: Subscription;

    constructor() { }
  
    ngOnInit(): void {
        if(this.activator != null) {
            this.active = this.activator.active;
            this.activatorSubscription = this.activator.getActivationObservable().subscribe(state => this.setActive(state));
          } else {
            this.active = true;
          }
      
        this.componentInit();
    }

    ngOnDestroy(): void {
        if(this.activatorSubscription != null) {
          this.activatorSubscription.unsubscribe();
        }
        this.componentDestroy();
    }

    abstract componentInit();

    abstract componentDestroy();

    abstract onActivationEvent(state: boolean);

    setActive(state: boolean) {
        this.active = state;
        this.onActivationEvent(state);
    }
}