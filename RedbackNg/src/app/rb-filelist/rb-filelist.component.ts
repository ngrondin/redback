import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbFile } from 'app/datamodel';
import { ApiService } from 'app/api.service';

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
    private apiService: ApiService
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
}
