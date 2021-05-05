import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { DataService } from 'app/services/data.service';
import { RbObject, RbFile } from 'app/datamodel';
import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';
import { RbSetComponent } from 'app/abstract/rb-set';

@Component({
  selector: 'rb-fileset',
  templateUrl: './rb-fileset.component.html',
  styleUrls: ['./rb-fileset.component.css']
})
export class RbFilesetComponent extends RbSetComponent {
  public fileList: RbFile[] = [];
  public selectedFile: RbFile;
  public uploader: FileUploader;
  public filesLoading: boolean;
  public initiated: boolean = false;
  public uploadProgress: number = -1;
  public overrideRbObject: RbObject;

  constructor(
    private dataService: DataService,
    private apiService: ApiService
  ) {
    super();
    this.uploader = new FileUploader({});
    this.uploader.response.subscribe( (res: any) => this.afterUpload(res) );
   }

  setInit() {
    this.initiated = true;
  }

  setDestroy() {
  }

  onDatasetEvent(event: any) {
    if(this.active == true) {
      if(event == 'select' || event == 'loaded') {
        this.refresh();
      } else if(event == 'cleared') {
        this.clear();
      }
    }
  }

  onActivationEvent(state: any) {
    state == true ? this.refresh() : this.clear();
  }

  @Input('overrideobject') set overrideobject(o : RbObject) {
    this.overrideRbObject = o;
    this.refresh();
  }
  

  get relatedObject() : RbObject {
    return this.overrideRbObject != null ? this.overrideRbObject : this.rbObject;
  }

  public reset() {
    if(this.active) {
      this.refresh()
    }
  }

  public clear() {
    this.fileList = [];
  }

  public refresh() {
    if(this.relatedObject != null) {
      this.dataService.listFiles(this.relatedObject.objectname, this.relatedObject.uid).subscribe(
        data => this.setData(data)
      );
      this.filesLoading = true;
    }
  }

  private setData(data: RbFile[]) {
    this.fileList = data;
    this.filesLoading = false;
    /*if(this.fileList.length == 1) {
      this.selectedFile = this.fileList[0];
    }*/
  }

  public uploadFile(file: File) {
    this.apiService.uploadFile(file, this.relatedObject.objectname, this.relatedObject.uid).subscribe(
      (prog) => this.uploadProgress = prog,
      (error) => { },
      () => this.afterUpload(null)
    );
  }
  
  public afterUpload(resp: any) {
    this.refresh();
  }
  
  public select(file: RbFile) {
    this.selectedFile = file;
  }

}
