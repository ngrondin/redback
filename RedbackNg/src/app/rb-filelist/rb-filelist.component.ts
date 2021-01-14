import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbFile } from 'app/datamodel';
import { ApiService } from 'app/services/api.service';
import { DomSanitizer } from '@angular/platform-browser';
import { RbFilesetDirective } from 'app/rb-fileset/rb-fileset.directive';

@Component({
  selector: 'rb-filelist',
  templateUrl: './rb-filelist.component.html',
  styleUrls: ['./rb-filelist.component.css']
})
export class RbFilelistComponent implements OnInit {
  @Input('fileset') fileset: RbFilesetDirective;
  @Input('downloadOnSelect') downloadOnSelect: boolean;
  @Input('details') showDetails: boolean = true;
  @Input('isLoading') isLoading: boolean;

  hasFileOver: boolean = false;

  constructor(
    private apiService: ApiService,
    private domSanitizer: DomSanitizer    
  ) { 

  }

  get list(): RbFile[] {
    return this.fileset != null ? this.fileset.list : [];
  }

  get selectedFile(): RbFile {
    return this.fileset != null ? this.fileset.selectedFile : null;
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    
  }

  select(file: RbFile) {
    this.fileset.select(file);
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
