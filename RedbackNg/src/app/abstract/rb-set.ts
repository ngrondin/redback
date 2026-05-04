import { Component } from "@angular/core";
import { Input } from "@angular/core";
import { RbContainerComponent } from "./rb-container";

@Component({template: ''})
export abstract class RbSetComponent extends RbContainerComponent {
    @Input('object') objectname: string;
    @Input('basefilter') baseFilter: any;
    @Input('master') master: any;
    @Input('requiresuserfilter') requiresuserfilter: boolean = false;
    @Input('ignoretarget') ignoretarget: boolean = false;
    
    containerInit(): void {
        this.setInit();
    }

    containerDestroy() : void  {
        this.setDestroy();
    }

    abstract setInit();

    abstract setDestroy();

    //abstract onDataTargetEvent(dt: DataTarget);

    public abstract refreshData();

    public abstract clear();

    /*setDataTarget(dt: DataTarget) {
        if(this.ignoretarget == false && dt != this.dataTarget) {
            this.dataTarget = dt;
            this.onDataTargetEvent(dt);
        }
    };*/

    public filterSort(event: any) : boolean {
        return false;
    }

}