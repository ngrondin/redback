import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbFile } from 'app/datamodel';
import { ApiService } from 'app/services/api.service';
import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';

import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbFilesetComponent } from 'app/rb-fileset/rb-fileset.component';

@Component({
  selector: 'rb-filedrop',
  templateUrl: './rb-filedrop.component.html',
  styleUrls: ['./rb-filedrop.component.css']
})
export class RbFiledropComponent extends RbContainerComponent {
  @Input('fileset') fileset: RbFilesetComponent;

  @Output() dropped: EventEmitter<any> = new EventEmitter();

  hasFileOver: boolean = false;

  constructor(
    private apiService: ApiService
  ) {
    super();
  }

  get uploader() : FileUploader {
    return this.fileset != null ? this.fileset.uploader : null;
  }

  containerInit() {
  }

  containerDestroy() {
  }

  onDatasetEvent(event: string) {
  }

  onActivationEvent(state: boolean) {
  }

  fileOver(event: any) {
    if(this.hasFileOver != event) {
      this.hasFileOver = event;
    }
  }

  fileDropped(event: any) {
    this.fileset.upload(event);
    //this.dropped.emit(event);
  }
 
}
