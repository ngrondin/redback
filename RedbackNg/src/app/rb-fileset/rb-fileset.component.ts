import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { DataService } from 'app/services/data.service';
import { RbObject, RbFile } from 'app/datamodel';
//import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';
import { RbSetComponent } from 'app/abstract/rb-set';

@Component({
  selector: 'rb-fileset',
  templateUrl: './rb-fileset.component.html',
  styleUrls: ['./rb-fileset.component.css']
})
export class RbFilesetComponent extends RbSetComponent {
  public fileList: RbFile[] = [];
  public selectedFile: RbFile;
  public filesLoading: boolean;
  public uploadProgress: number = -1;
  public overrideRbObject: RbObject;

  constructor(
    private dataService: DataService,
    private apiService: ApiService
  ) {
    super();
   }

  setInit() {
    this.refreshData();
  }

  setDestroy() {
  }

  onDatasetEvent(event: any) {
    if(this.active == true) {
      if(event == 'select' || event == 'load') {
        this.refreshData();
      } else if(event == 'clear') {
        this.clear();
      }
    }
  }

  onActivationEvent(state: any) {
    state == true ? this.refreshData() : this.clear();
  }

  onDataTargetEvent(dt: any) {
    
  }

  @Input('overrideobject') set overrideobject(o : RbObject) {
    this.overrideRbObject = o;
    this.refreshData();
  }
  

  get relatedObject() : RbObject {
    return this.overrideRbObject != null ? this.overrideRbObject : this.rbObject;
  }

  public clear() {
    this.fileList = [];
  }

  public refreshData() {
    if(this.active && this.relatedObject != null) {
      this.dataService.listFiles(this.relatedObject.objectname, this.relatedObject.uid).subscribe(
        data => this.setData(data)
      );
      this.filesLoading = true;
    }
  }

  private setData(data: RbFile[]) {
    this.fileList = data;
    this.filesLoading = false;
  }

  public uploadFile(file: File) {
    this.apiService.uploadFile(file, this.relatedObject.objectname, this.relatedObject.uid).subscribe(
      (prog) => this.uploadProgress = prog.value,
      (error) => { },
      () => this.afterUpload(null)
    );
  }
  
  public afterUpload(resp: any) {
    this.uploadProgress = -1;
    this.refreshData();
  }
  
  public select(file: RbFile) {
    this.selectedFile = file;
  }

  public delete(file: RbFile) {
    this.apiService.unlinkFile(file.fileUid, this.relatedObject.objectname, this.relatedObject.uid).subscribe(next => this.refreshData());
  }
}
