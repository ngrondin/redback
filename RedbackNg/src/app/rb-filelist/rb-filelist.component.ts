import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbFile } from 'app/datamodel';
import { ApiService } from 'app/api.service';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'rb-filelist',
  templateUrl: './rb-filelist.component.html',
  styleUrls: ['./rb-filelist.component.css']
})
export class RbFilelistComponent implements OnInit {
  @Input('list') list: RbFile[];
  @Input('selectedFile') selectedFile: RbFile;
  @Input('downloadOnSelect') downloadOnSelect: boolean;
  @Input('isLoading') isLoading: boolean;

  @Output() selected: EventEmitter<any> = new EventEmitter();

  hasFileOver: boolean = false;

  constructor(
    private apiService: ApiService,
    private domSanitizer: DomSanitizer    
  ) { 

  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    
  }

  select(file: RbFile) {
    this.selected.emit(file);
    if(this.downloadOnSelect) {
      window.open(this.apiService.baseUrl + '/' + this.apiService.fileService + '?fileuid=' + file.fileUid);
    }
  }

  getDisplayFileName(file: RbFile) {
    if(file.fileName.length > 40) {
      return file.fileName.substr(0, 40) + "...";
    } else {
      return file.fileName;
    }
  }

  getBase64Thumbnail(file: RbFile) {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(file.thumbnail);
  }
}
