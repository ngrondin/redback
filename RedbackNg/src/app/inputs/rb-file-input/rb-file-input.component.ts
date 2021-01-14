import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { RbInputCommonComponent } from 'app/inputs/rb-input-common/rb-input-common.component';
import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';
import { ApiService } from 'app/services/api.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'rb-file-input',
  templateUrl: './rb-file-input.component.html',
  styleUrls: ['./rb-file-input.component.css']
})
export class RbFileInputComponent extends RbInputCommonComponent implements OnInit {

  @Output() dropped: EventEmitter<any> = new EventEmitter();

  hasFileOver: boolean = false;
  public uploader: FileUploader;
  defaultIcon: string = 'description';

  constructor(
    private apiService: ApiService,
    private domSanitizer: DomSanitizer    
  ) {
    super();
    this.uploader = new FileUploader({});
    this.uploader.response.subscribe( (res: any) => this.fileUploaded(res) );
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges) {
    
  }

  get fileUid() : String{
    if(this.rbObject != null) {
      let val = this.rbObject.get(this.attribute);
      if(val != null)
        return val.fileuid;
    } 
    return null;
  } 

  get thumbnail() {
    if(this.rbObject != null) {
      let val = this.rbObject.get(this.attribute);
      if(val != null && val.thumbnail != null) {
        return this.domSanitizer.bypassSecurityTrustResourceUrl(val.thumbnail);;
      }
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



  fileOver(event: any) {
    if(this.hasFileOver != event) {
      this.hasFileOver = event;
    }
  }

  fileDropped(event: any) {
    this.upload(event);
  }

  upload(evetn: any) {
    let options : FileUploaderOptions = {};
    options.url = this.apiService.baseUrl + '/' + this.apiService.fileService;
    options.disableMultipart = false;
    this.uploader.setOptions(options);
    this.uploader.uploadAll();
  }

  fileUploaded(res: any) {
    if(this.rbObject != null) {
      this.rbObject.setValue(this.attribute, JSON.parse(res));
      this.change.emit(this.editedValue);
    }    
  }

  openFile() {
    if(this.rbObject != null) {
      let val = this.rbObject.get(this.attribute);
      if(val.fileuid != null) {
        window.open(this.apiService.baseUrl + '/' + this.apiService.fileService + '?fileuid=' + val.fileuid);
      }
    }     
  }
}
