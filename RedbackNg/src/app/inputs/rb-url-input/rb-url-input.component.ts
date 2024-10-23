import { Component } from '@angular/core';
import { RbStringInputComponent } from '../rb-string-input/rb-string-input.component';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { HttpClient } from '@angular/common/http';
import { ApiService } from 'app/services/api.service';

@Component({
  selector: 'rb-url-input',
  templateUrl: './rb-url-input.component.html',
  styleUrls: ['../abstract/rb-field-input.css', './rb-url-input.component.css']
})
export class RbUrlInputComponent extends RbStringInputComponent {

  constructor(
    private apiService: ApiService
  ) {
    super();
    this.defaultIcon = "link";
  }

  public get urlvalue() : string {
    let val = this.value;
    if(typeof val == 'object' && val != null) {
      return val.url;
    } else {
      return val;
    }
  }

  public get iframeurl() : string {
    return typeof this.value == 'object' && this.value != null && this.value.iframeurl != null ? this.value.iframeurl : null;
  }

  public get title() : string {
    return typeof this.value == 'object' && this.value != null && this.value.title != null ? this.value.title : null;
  }

  public get image() : string {
    return typeof this.value == 'object' && this.value != null && this.value.image != null ? this.value.image : null;
  }

  public get description() : string {
    return typeof this.value == 'object' && this.value != null && this.value.description != null ? this.value.description : null;
  }

  public getPersistedDisplayValue(): any {
    return this.urlvalue;
  }

  public initEditedValue() {
    this.editedValue = this.getPersistedDisplayValue();
  }

  public finishEditing() {
    this.editedValue = {url: this.editedValue};
    super.finishEditing();
    setTimeout(() => this.getPreview(), 0);
  }

  private getPreview() {
    this.apiService.getPreviewUrl(this.urlvalue).subscribe(data => {
      this.commit(data);
    })
  }

  public click() {
    window.open(this.urlvalue);
  }

}
