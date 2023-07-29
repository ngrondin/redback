import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges, HostListener } from '@angular/core';
import { RbFile } from 'app/datamodel';
import { ApiService } from 'app/services/api.service';

import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbFilesetComponent } from 'app/rb-fileset/rb-fileset.component';

@Component({
  selector: 'rb-filedrop',
  templateUrl: './rb-filedrop.component.html',
  styleUrls: ['./rb-filedrop.component.css']
})
export class RbFiledropComponent extends RbContainerComponent {
  @Input('fileset') fileset: RbFilesetComponent;
  @Input('editable') editable: boolean = true;

  @Output() dropped: EventEmitter<any> = new EventEmitter();

  hasFileOver: boolean = false;

  constructor(
    private apiService: ApiService
  ) {
    super();
  }

  containerInit() {
  }

  containerDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  @HostListener('drop', ['$event'])
  public drop(event: any) {
    if(this.editable) {
      event.preventDefault();
      event.stopPropagation();
      console.log("File dropped");
      if (event.dataTransfer != null && event.dataTransfer.items) {
        for (var i = 0; i < event.dataTransfer.items.length; i++) {
          if (event.dataTransfer.items[i].kind === 'file') {
            var file = event.dataTransfer.items[i].getAsFile();
            this.fileset.uploadFile(file);
          }
        }
      }
      this.hasFileOver = false;
    }
  }
  
  @HostListener('dragover', ['$event'])
  dragover(event: any) {
    if(this.editable) {
      event.stopPropagation();
      event.preventDefault();
      this.hasFileOver = true;
    }
  }

  @HostListener('dragleave', ['$event'])
  dragleave(event: any) {
    if(this.editable) {
      this.hasFileOver = false;
    }
  }
}
