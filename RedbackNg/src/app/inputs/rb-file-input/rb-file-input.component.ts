import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HostBinding } from '@angular/core';
import { RbInputComponent } from '../abstract/rb-input';
import { HostListener } from '@angular/core';
import { LogService } from 'app/services/log.service';
import { RbFileviewerComponent } from 'app/rb-fileviewer/rb-fileviewer.component';
import { MatDialog } from '@angular/material/dialog';
import { ErrorService } from 'app/services/error.service';

@Component({
  selector: 'rb-file-input',
  templateUrl: './rb-file-input.component.html',
  styleUrls: ['./rb-file-input.component.css']
})
export class RbFileInputComponent extends RbInputComponent  {

  @Input('fileuidattribute') fileuidattribute?: string;
  @Input('mimeattribute') mimeattribute?: string;
  @Input('thumbnailattribute') thumbnailattribute?: string;
  @Input('width') width?: number;
  @Input('height') height?: number;
  @Input('shrinkborder') shrinkborder: boolean = true;
  @Input('validextensions') validextensions: string[] | null = null;
  @Output() dropped: EventEmitter<any> = new EventEmitter();
  @HostBinding('style.width') get hostWidth() { return (this.width != null ? (('min(' + (0.88 * this.width) + 'vw, ' + (17 * this.width) + 'px)')): 'min(4.5vw, 87px)');}
  @HostBinding('style.height') get hostHeight() { return (this.height != null ? (('min(' + (0.88 * this.height) + 'vw, ' + (17 * this.width!) + 'px)')): 'min(3vw, 57px)');}

  hasFileOver: boolean = false;
  defaultIcon: string = 'description';
  uploadProgress: number = -1;


  constructor(
    private apiService: ApiService,
    private logService: LogService,
    private errorService: ErrorService,
    public dialog: MatDialog,
    private domSanitizer: DomSanitizer    
  ) {
    super();
  }

  inputInit() {
  }

  public get displayvalue(): any {
    let val = this.thumbnail;
    return val != null ? this.domSanitizer.bypassSecurityTrustResourceUrl(val) : null;
  }

  get fileUid() : string|null {
    return this.getMetaValue("fileuid");
  } 

  get mime() : string|null {
    return this.getMetaValue("mime");
  } 

  get thumbnail() : string|null {
    return this.getMetaValue("thumbnail");
  }

  getMetaValue(field: string) : string|null {
    let val = null;
    if(this.rbObject != null) {
      let fieldAttributeName: string|undefined = (this as any)[field + "attribute"];
      if(fieldAttributeName != null) {
        val = this.rbObject.get(fieldAttributeName);
      } else if(this.attribute != null) {
        let struct = this.rbObject.get(this.attribute);
        if(struct != null && struct[field] != null) {
          val = struct[field];
        }
      }
    } 
    return val;    
  }

  hasThumbnail(): boolean {
    return this.thumbnail != null;
  }

  @HostListener('drop', ['$event'])
  public drop(event: any) {
    event.preventDefault();
    event.stopPropagation();
    this.logService.info("File dropped, items " + event.dataTransfer?.item?.length + ", files " + event.dataTransfer?.files?.length);
    if (event.dataTransfer != null && event.dataTransfer.items) {
      for (var i = 0; i < event.dataTransfer.items.length; i++) {
        if (event.dataTransfer.items[i].kind === 'file') {
          var file = event.dataTransfer.items[i].getAsFile();
          this.uploadFile(file);
        }
      }
    }
    this.hasFileOver = false;
  }

  uploadFile(file: File) {
    let ext = file.name.substring(file.name.lastIndexOf(".") + 1);
    if(this.validextensions != null && this.validextensions.indexOf(ext) ==-1) {
      this.errorService.showError("Invalid file type");
      return;
    }
    this.apiService.uploadFile(file).subscribe({
      next: (resp) => {
        if(resp.type == "result") this.fileUploaded(resp.result);
      },
      error: (error) => {
        this.errorService.showError(error)
      }
    } );
  }

  fileUploaded(res: any) {
    if(this.rbObject != null) {
      if(this.fileuidattribute != null) {
        this.rbObject.setValue(this.fileuidattribute, res.fileuid);
      }
      if(this.mimeattribute != null) {
        this.rbObject.setValue(this.mimeattribute, res.mime);
      }
      if(this.thumbnailattribute != null) {
        this.rbObject.setValue(this.thumbnailattribute, res.thumbnail);
      }
    }
    this.commit(res);
  }

  click() {
    if(this.fileUid != null) {
      this.openFile();
    } else {
      this.chooseFile();
    }
  }

  openFile() {
    if(this.fileUid) {
      let isImg = this.mime != null && this.mime.startsWith("image/");
      if(!isImg) {
        window.open(this.apiService.baseUrl + '/' + this.apiService.fileService + '?fileuid=' + this.fileUid);
      } else {
        this.dialog.open(RbFileviewerComponent, {
          data: {
            fileUid: this.fileUid
          },
          autoFocus: false,
          restoreFocus: false
        });
      }
    }     
  }

  chooseFile() {
    let input = document.createElement("input");
    input.type = 'file';
    input.onchange = (event: any) => {
      if(event != null && event.target != null) {
        var files = event.target['files'];
        for(var file of files) {
          this.uploadFile(file);
        }
      }
    }
    input.click();
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
