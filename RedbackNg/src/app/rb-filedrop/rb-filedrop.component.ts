import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbFile } from 'app/datamodel';
import { ApiService } from 'app/api.service';
import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';

@Component({
  selector: 'rb-filedrop',
  templateUrl: './rb-filedrop.component.html',
  styleUrls: ['./rb-filedrop.component.css']
})
export class RbFiledropComponent implements OnInit {
  @Input('uploader') uploader: FileUploader;

  @Output() dropped: EventEmitter<any> = new EventEmitter();

  hasFileOver: boolean = false;

  constructor(
    private apiService: ApiService
  ) { 

  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    
  }
  
  fileOver(event: any) {
    if(this.hasFileOver != event) {
      this.hasFileOver = event;
    }
  }

  fileDropped(event: any) {
    this.dropped.emit(event);
  }
 
}
