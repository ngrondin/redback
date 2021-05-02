import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges, HostListener } from '@angular/core';
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
/*
  fileOver(event: any) {
    if(this.hasFileOver != event) {
      this.hasFileOver = event;
    }
  }

  fileDropped(event: any) {
    this.fileset.upload(event);
    //this.dropped.emit(event);
  }
*/
  @HostListener('drop', ['$event'])
  public drop(event: any) {
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
  
  @HostListener('dragover', ['$event'])
  dragover(event: any) {
    event.stopPropagation();
    event.preventDefault();
    this.hasFileOver = true;
  }

  @HostListener('dragleave', ['$event'])
  dragleave(event: any) {
    this.hasFileOver = false;
  }
}
