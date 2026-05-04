import { Component, ComponentRef, HostBinding, Input, ViewChild, ViewContainerRef } from '@angular/core';
import { RbActivatorComponent } from 'app/abstract/rb-activator';
import { RbComponent } from 'app/abstract/rb-component';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { sleep, VirtualSelector } from 'app/helpers';
import { BuildService } from 'app/services/build.service';

@Component({
  selector: 'rb-repeater',
  templateUrl: './rb-repeater.component.html',
  styleUrls: ['./rb-repeater.component.css']
})
export class RbRepeaterComponent extends RbActivatorComponent {

  @Input('repeat') repeat: any;
  @Input('direction') direction = 'row';
  @ViewChild('content', { read: ViewContainerRef, static: true }) content?: ViewContainerRef;

  @HostBinding('style.flex-direction') get dir() { return this.direction == 'col' ? 'column' : 'row';}
  
  data: any[] = [];

  constructor(
    private buildService: BuildService,
  ) {
    super();
  }
  
  activatorInit() {
  }

  activatorDestroy() {
  }

  onActivationEvent(state: boolean) {
    if(state == true) {
      //this.calc()
    }
  }

  onDatasetEvent(event: any) {
    if(event.event == 'load') {
      this.calc()
    }
  }

  async calc() {
    console.log("Repeater activating");
    this.data = [];
    if(this.content != null && this.dataset != null) {
      while(this.content.length > 0) {
        this.content.detach(0);
      }
      for(var object of this.dataset.list) {
        for(var item of this.repeat) {
          var vs = new VirtualSelector();
          vs.selectedObject = object;
          var context: any = {dataset: this.dataset, datasetgroup: this.datasetgroup, virtualselector:vs, activator: this};
          this.buildService.buildConfigRecursive(this.content, item, context);
        }
      }
      await sleep(1); //This is to make sure the components are fully attached and initiated
      this.activate()         
    }
  }
}
