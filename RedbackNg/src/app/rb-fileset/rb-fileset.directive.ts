import { Directive, Input, SimpleChanges } from '@angular/core';
import { RbObject, RbFile } from 'app/datamodel';
import { DataService } from 'app/services/data.service';
import { FileUploader, FileUploaderOptions } from 'ng2-file-upload';
import { ApiService } from 'app/services/api.service';
import { RbDatasetComponent } from 'app/rb-dataset/rb-dataset.component';

@Directive({
  selector: 'rb-fileset',
  exportAs: 'fileset'
})
export class RbFilesetDirective {
  @Input('dataset') relatedDataset: RbDatasetComponent;
  @Input('relatedObject') _relatedObject: RbObject;
  @Input('active') active: boolean;

  public list: RbFile[] = [];
  public selectedFile: RbFile;
  public uploader: FileUploader;
  public isLoading: boolean;
  public initiated: boolean = false;

  constructor(
    private dataService: DataService,
    private apiService: ApiService
  ) {
    this.uploader = new FileUploader({});
    this.uploader.response.subscribe( (res: any) => this.afterUpload(res) );
   }

  get relatedObject() : RbObject {
    return this.relatedDataset != null ? this.relatedDataset.selectedObject : this._relatedObject != null ? this._relatedObject : null;
  }

  ngOnInit() {
    this.refreshData();
    this.initiated = true;
  }

  ngOnChanges(changes: SimpleChanges) {
    if(this.initiated) {
      if(this.active)
        this.refreshData();
    else
      this.list = [];
    }
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
