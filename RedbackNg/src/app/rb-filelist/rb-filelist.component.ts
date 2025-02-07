import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbFile } from 'app/datamodel';
import { ApiService } from 'app/services/api.service';
import { DomSanitizer } from '@angular/platform-browser';
import { RbFilesetComponent } from 'app/rb-fileset/rb-fileset.component';
import { RbComponent } from 'app/abstract/rb-component';
import { UserprefService } from 'app/services/userpref.service';
import { Formatter } from 'app/helpers';
import { MatDialog } from '@angular/material/dialog';
import { RbFileviewerComponent } from 'app/rb-fileviewer/rb-fileviewer.component';

@Component({
  selector: 'rb-filelist',
  templateUrl: './rb-filelist.component.html',
  styleUrls: ['./rb-filelist.component.css']
})
export class RbFilelistComponent extends RbComponent {
  @Input('fileset') fileset: RbFilesetComponent;
  @Input('downloadOnSelect') downloadOnSelect: boolean = false;
  @Input('details') showDetails: boolean = true;
  
  
  hasFileOver: boolean = false;
  hovering: RbFile = null;

  constructor(
    private apiService: ApiService,
    private domSanitizer: DomSanitizer,
    public userpref: UserprefService,
    public dialog: MatDialog
  ) {
    super();
  }

  get list(): RbFile[] {
    return this.fileset != null ? this.fileset.fileList : [];
  }

  get selectedFile(): RbFile {
    return this.fileset != null ? this.fileset.selectedFile : null;
  }

  get isLoading(): boolean {
    return this.fileset != null ? this.fileset.filesLoading : false;
  }

  get uploadProgress(): number {
    return this.fileset != null ? this.fileset.uploadProgress : -1;
  }

  get editable(): boolean {
    return this.fileset.editable;
  }

  componentInit() {
  }

  componentDestroy() {
  }

  onActivationEvent(state: boolean) {
  }

  select(file: RbFile) {
    this.fileset.select(file);
    let isImg = file.mime.startsWith("image/");
    if(this.downloadOnSelect || !isImg) {
      window.open(this.apiService.baseUrl + '/' + this.apiService.fileService + '?fileuid=' + file.fileUid);
    } else {
      this.dialog.open(RbFileviewerComponent, {
        data: {
          fileUid: file.fileUid
        },
        autoFocus: false,
        restoreFocus: false
      });
    }
  }

  getDisplayFileName(file: RbFile) {
    if(file.fileName.length > 40) {
      return file.fileName.substr(0, 40) + "...";
    } else {
      return file.fileName;
    }
  }

  getDateString(file: RbFile) {
    return Formatter.formatDateTime(file.date);
  }

  getBase64Thumbnail(file: RbFile) {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(file.thumbnail);
  }

  startHovering(file) {
    this.hovering = file;
  }

  endHovering(file) {
    if(this.hovering == file) this.hovering = null;
  }

  delete(event, file) {
    this.fileset.delete(file);
    event.stopPropagation();
    event.preventDefault();
  }


}
