import { Component, Input, ViewChild } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';
import { FileReferenceResolver, HtmlParser } from 'app/helpers';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AppInjector } from 'app/app.module';
import { ApiService } from 'app/services/api.service';

declare var Quill: any;

@Component({
  selector: 'rb-richtext-input',
  templateUrl: './rb-richtext-input.component.html',
  styleUrls: ['./rb-richtext-input.component.css']
})
export class RbRichtextInputComponent extends RbFieldInputComponent {
  @Input('allowswitch') allowswitch: boolean = false;
  @Input('wraphtml') wraphtml: boolean = false;
  editor: any = null;
  codeconfig: any = {
    printMargin: false
  }
  mode: string = 'editor';
  codeSource: string = null;
  safeHtml: SafeHtml = null;
  domSanitizer: DomSanitizer = null;
  uniqueId: number = Math.round(1000 * Math.random());

  constructor(
    public apiService: ApiService
  ) {
    super();
    this.defaultIcon = "description";
    this.domSanitizer = AppInjector.get(DomSanitizer); 
  }

  inputInit() {
    super.inputInit();    
  }

  ngAfterViewInit() {
    setTimeout(() => this.initiateQuill(), 100);
    //this.initiateQuill();
  }

  onActivationEvent(state: boolean) {
    super.onActivationEvent(state);
    //this.initiateQuill();
  }


  onDatasetEvent(event: string) {
    if(this.editor != null) {
      if(this.editor.root != null && this.editor.root.innerHTML != this.value) {
        this.editor.root.innerHTML = this.value;  
      }
      //this.editor.enable(!this.readonly);
    }
    if(this.codeSource != this.value) {
      this.codeSource = HtmlParser.stringify(HtmlParser.parse(this.value), true);
    } 
    let _safeHtml = this.domSanitizer.bypassSecurityTrustHtml(FileReferenceResolver.resolve(this.value ?? '', this.apiService.fileService));
    if(this.safeHtml != _safeHtml) {
      this.safeHtml = _safeHtml;
    }  
  }

  initiateQuill() {
    if(/*this.active && */this.editor == null) {
      this.editor = new Quill('#quilleditor_' + this.uniqueId, {
        modules: {
          toolbar: {
            container: "#quilltoolbar_" + this.uniqueId
          }
        },
        theme: 'snow'
      });
      this.editor.root.addEventListener("focus", ($event) => {
        this.onFocus($event);
      });      
      this.editor.root.addEventListener("blur", ($event) => {
        this.onBlur($event);
      });  
      this.onDatasetEvent('select');
    }
  }

  public getPersistedDisplayValue(): any {
    return null;
  }

  public getEditingDisplayValue(): any {
    return null;
  }
  
  public setDisplayValue(val: any) {
   
  }
  
  public onKeydown(event: any) {
    if(event.keyCode == 27) {
      this.cancelEditing();
    } 
  }

  public finishEditing() {
    let rawValue = this.mode == 'editor' ? (this.editor != null && this.editor.root != null ? this.editor.root.innerHTML : null) : this.codeSource;
    this.editedValue = rawValue != null && rawValue != '' ? HtmlParser.stringify(HtmlParser.parse(rawValue), false, 0, this.wraphtml) : null;
    this.commit(this.editedValue);
    super.finishEditing();
  }

  public toggleMode() {
    if(this.allowswitch) {
      this.mode = (this.mode == 'editor' ? 'code' : 'editor');
    }
  }

}


