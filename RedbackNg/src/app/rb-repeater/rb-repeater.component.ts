import { Component, HostBinding, Input, ViewChild, ViewContainerRef } from '@angular/core';
import { RbDataObserverComponent } from 'app/abstract/rb-dataobserver';
import { VirtualSelector } from 'app/helpers';
import { BuildService } from 'app/services/build.service';

@Component({
  selector: 'rb-repeater',
  templateUrl: './rb-repeater.component.html',
  styleUrls: ['./rb-repeater.component.css']
})
export class RbRepeaterComponent extends RbDataObserverComponent {
  @Input('repeat') repeat: any;
  @Input('direction') direction = 'row';
  @ViewChild('content', { read: ViewContainerRef, static: true }) content: ViewContainerRef;

  @HostBinding('style.flex-direction') get dir() { return this.direction == 'col' ? 'column' : 'row';}
  
  data: any[];

  constructor(
    private buildService: BuildService,
  ) {
    super();
  }
  
  dataObserverInit() {
  }

  dataObserverDestroy() {
  }

  onDatasetEvent(event: any) {
    if(event.event == 'load') {
      this.calc()
    }
  }

  onActivationEvent(state: boolean) {
    if(state == true) {
      this.calc()
    }
  }

  calc() {
    this.data = [];
    while(this.content.length > 0) {
      this.content.detach(0);
    }
    for(var object of this.dataset.list) {
      for(var item of this.repeat) {
        var vs = new VirtualSelector();
        vs.selectedObject = object;
        var context: any = {dataset: this.dataset, datasetgroup: this.datasetgroup, virtualselector:vs};
        this.buildService.buildConfigRecursive(this.content, item, context);
      }
    }
  }
}
