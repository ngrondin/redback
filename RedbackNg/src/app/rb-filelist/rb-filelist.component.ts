import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbFile } from 'app/datamodel';
import { ApiService } from 'app/services/api.service';
import { DomSanitizer } from '@angular/platform-browser';
import { RbFilesetComponent } from 'app/rb-fileset/rb-fileset.component';
import { RbComponent } from 'app/abstract/rb-component';
import { UserprefService } from 'app/services/userpref.service';

@Component({
  selector: 'rb-filelist',
  templateUrl: './rb-filelist.component.html',
  styleUrls: ['./rb-filelist.component.css']
})
export class RbFilelistComponent extends RbComponent {
  @Input('fileset') fileset: RbFilesetComponent;
  @Input('downloadOnSelect') downloadOnSelect: boolean = true;
  @Input('details') showDetails: boolean = true;
  @Input('isLoading') isLoading: boolean;

  hasFileOver: boolean = false;

  constructor(
    private apiService: ApiService,
    private domSanitizer: DomSanitizer,
    public userpref: UserprefService
  ) {
    super();
  }

  get list(): RbFile[] {
    return this.fileset != null ? this.fileset.list : [];
  }

  get selectedFile(): RbFile {
    return this.fileset != null ? this.fileset.selectedFile : null;
  }

  componentInit() {
  }

  componentDestroy() {
  }

  onActivationEvent(state: boolean) {
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
