import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HostBinding } from '@angular/core';
import { RbInputComponent } from '../abstract/rb-input';
import { HostListener } from '@angular/core';
import { LogService } from 'app/services/log.service';
import { RbFileviewerComponent } from 'app/rb-fileviewer/rb-fileviewer.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'rb-file-input',
  templateUrl: './rb-file-input.component.html',
  styleUrls: ['./rb-file-input.component.css']
})
export class RbFileInputComponent extends RbInputComponent  {

  @Input('width') width: number;
  @Input('height') height: number;
  @Input('shrinkborder') shrinkborder: boolean = true;
  @Output() dropped: EventEmitter<any> = new EventEmitter();
  @HostBinding('style.width') get hostWidth() { return (this.width != null ? ((0.88 * this.width) + 'vw'): '4.5vw');}
  @HostBinding('style.height') get hostHeight() { return (this.height != null ? ((0.88 * this.height) + 'vw'): '3vw');}

  hasFileOver: boolean = false;
  defaultIcon: string = 'description';
  uploadProgress: number = -1;


  constructor(
    private apiService: ApiService,
    private logService: LogService,
    public dialog: MatDialog,
    private domSanitizer: DomSanitizer    
  ) {
    super();
  }

  inputInit() {
  }

  public get displayvalue(): any {
    if(this.rbObject != null) {
      let val = this.rbObject.get(this.attribute);
      if(val != null && val.thumbnail != null) {
        return this.domSanitizer.bypassSecurityTrustResourceUrl(val.thumbnail);;
      }
    } 
    return null;
  }

  get fileUid() : String{
    if(this.rbObject != null) {
      let val = this.rbObject.get(this.attribute);
      if(val != null)
        return val.fileuid;
    } 
    return null;
  } 


  hasThumbnail(): boolean {
    if(this.rbObject != null) {
      let val = this.rbObject.get(this.attribute);
      if(val != null && val.thumbnail != null) {
        return true;
      }
    } 
    return false;
  }

  @HostListener('drop', ['$event'])
  public drop(event: any) {
    event.preventDefault();
    event.stopPropagation();
    this.logService.info("File dropped");
    if (event.dataTransfer != null && event.dataTransfer.items) {
      for (var i = 0; i < event.dataTransfer.items.length; i++) {
        if (event.dataTransfer.items[i].kind === 'file') {
          var file = event.dataTransfer.items[i].getAsFile();
          this.apiService.uploadFile(file, null, null).subscribe(
            (resp) => {
              if(resp.type == "result") this.fileUploaded(resp.result);
            },
            (error) => { }
          );
        }
      }
    }
    this.hasFileOver = false;
  }

  upload(evetn: any) {

  }

  fileUploaded(res: any) {
    this.commit(res);
  }

  openFile() {
    if(this.rbObject != null) {
      let val = this.rbObject.get(this.attribute);
      if(val.fileuid != null) {
        let isImg = val.mime != null && val.mime.startsWith("image/");
        if(!isImg) {
          window.open(this.apiService.baseUrl + '/' + this.apiService.fileService + '?fileuid=' + val.fileuid);
        } else {
          this.dialog.open(RbFileviewerComponent, {
            data: {
              fileUid: val.fileuid
            },
            autoFocus: false,
            restoreFocus: false
          });
        }
      }
    }     
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
