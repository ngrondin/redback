import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { ApiService } from 'app/services/api.service';
import { DataService } from 'app/services/data.service';
import { RbObject, RbFile } from 'app/datamodel';
import { RbContainerComponent } from 'app/abstract/rb-container';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';
import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';

@Component({
  selector: 'rb-fileset',
  templateUrl: './rb-fileset.component.html',
  styleUrls: ['./rb-fileset.component.css']
})
export class RbFilesetComponent extends RbContainerComponent {
  @Input('relatedObject') _relatedObject: RbObject;

  public list: RbFile[] = [];
  public selectedFile: RbFile;
  public uploader: FileUploader;
  public isLoading: boolean;
  public initiated: boolean = false;
  public active: boolean;

  constructor(
    private dataService: DataService,
    private apiService: ApiService
  ) {
    super();
    this.uploader = new FileUploader({});
    this.uploader.response.subscribe( (res: any) => this.afterUpload(res) );
   }

  containerInit() {
    this.refreshData();
    this.initiated = true;
  }

  containerDestroy() {
  }

  onDatasetEvent(event: any) {
  }

  onActivationEvent(event: any) {
  }

  get relatedObject() : RbObject {
    return this.dataset != null ? this.dataset.selectedObject : this._relatedObject != null ? this._relatedObject : null;
  }

  public refreshData() {
    if(this.relatedObject != null) {
      this.dataService.listFiles(this.relatedObject.objectname, this.relatedObject.uid).subscribe(
        data => this.setData(data)
      );
      this.isLoading = true;
    }
  }

  private setData(data: RbFile[]) {
    this.list = data;
    this.isLoading = false;
    if(this.list.length == 1) {
      this.selectedFile = this.list[0];
    }
  }

  public upload(evetn: any) {
    if(this.relatedObject != null) {
      let options : FileUploaderOptions = {};
      options.url = this.apiService.baseUrl + '/' + this.apiService.fileService;
      options.disableMultipart = false;
      options.additionalParameter = {
        "object" : this.relatedObject.objectname,
        "uid" : this.relatedObject.uid
      }
      this.uploader.setOptions(options);
      this.uploader.uploadAll();
    }
  }
  
  public afterUpload(resp: any) {
    this.refreshData();
  }
  
  public select(file: RbFile) {
    this.selectedFile = file;
  }

}
