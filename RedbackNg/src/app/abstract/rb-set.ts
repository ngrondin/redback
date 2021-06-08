import { Component } from "@angular/core";
import { Input } from "@angular/core";
import { DataTarget } from "app/datamodel";
import { RbContainerComponent } from "./rb-container";

@Component({template: ''})
export abstract class RbSetComponent extends RbContainerComponent {
    @Input('object') object: string;
    @Input('basefilter') baseFilter: any;
    @Input('master') master: any;
    @Input('requiresuserfilter') requiresuserfilter: boolean = false;
    @Input('ignoretarget') ignoretarget: boolean = false;
  
    dataTarget: DataTarget;
    
    constructor() {
        super();
    }
  
    containerInit(): void {
        this.setInit();
    }

    containerDestroy() : void  {
        this.setDestroy();
    }

    abstract setInit();

    abstract setDestroy();

    abstract onDataTargetEvent(dt: DataTarget);

    public abstract clear();

    setDataTarget(dt: DataTarget) {
        if(this.ignoretarget == false && dt != this.dataTarget) {
            this.dataTarget = dt;
            this.onDataTargetEvent(dt);
        }
    };
}