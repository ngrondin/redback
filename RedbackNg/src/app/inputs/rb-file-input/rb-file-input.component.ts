import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HostBinding } from '@angular/core';
import { RbInputComponent } from '../abstract/rb-input';
import { HostListener } from '@angular/core';

@Component({
  selector: 'rb-file-input',
  templateUrl: './rb-file-input.component.html',
  styleUrls: ['./rb-file-input.component.css']
})
export class RbFileInputComponent extends RbInputComponent  {

  @Input('width') width: number;
  @Input('height') height: number;
  @Output() dropped: EventEmitter<any> = new EventEmitter();
  @HostBinding('style.width.px') get hostWidth() { return this.width != null ? this.width : 100;}
  @HostBinding('style.height.px') get hostHeight() { return this.height != null ? this.height : 100;}

  hasFileOver: boolean = false;
  defaultIcon: string = 'description';
  uploadProgress: number = -1;


  constructor(
    private apiService: ApiService,
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

  public set displayvalue(val: any) {
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
    console.log("File dropped");
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
        window.open(this.apiService.baseUrl + '/' + this.apiService.fileService + '?fileuid=' + val.fileuid);
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
