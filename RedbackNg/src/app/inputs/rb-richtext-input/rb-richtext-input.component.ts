import { Component, Input, ViewChild } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';
import { HtmlParser } from 'app/helpers';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { AppInjector } from 'app/app.module';

declare var Quill: any;

@Component({
  selector: 'rb-richtext-input',
  templateUrl: './rb-richtext-input.component.html',
  styleUrls: ['./rb-richtext-input.component.css']
})
export class RbRichtextInputComponent extends RbFieldInputComponent {
  @Input('allowswitch') allowswitch: boolean = false;
  editor: any = null;
  editorconfig: any = {
    modules: {
      toolbar: {
        container: "#quilltoolbar"
      }
    },
    theme: 'snow'
  };
  codeconfig: any = {
    printMargin: false
  }
  mode: string = 'editor';
  codeSource: string = null;
  safeHtml: SafeHtml = null;
  domSanitizer: DomSanitizer = null;

  constructor() {
    super();
    this.defaultIcon = "description";
    this.domSanitizer = AppInjector.get(DomSanitizer);
  }

  ngAfterViewInit() {
    setTimeout(() => this.initiateQuill(), 100);
  }

  onActivationEvent(state: boolean) {
    super.onActivationEvent(state);
    this.initiateQuill();
  }


  onDatasetEvent(event: string) {
    if(this.editor != null) {
      if(this.editor.root != null && this.editor.root.innerHTML != this.value) {
        this.editor.root.innerHTML = this.value;  
      }
      this.editor.enable(!this.readonly);
    }
    if(this.codeSource != this.value) {
      this.codeSource = HtmlParser.stringify(HtmlParser.parse(this.value), true);
    } 
    let _safeHtml = this.domSanitizer.bypassSecurityTrustHtml(this.value);
    if(this.safeHtml != _safeHtml) {
      this.safeHtml = _safeHtml;
    }  
  }

  initiateQuill() {
    if(this.active && this.editor == null) {
      this.editor = new Quill('#quilleditor', this.editorconfig);
      this.editor.root.addEventListener("focus", ($event) => {
        this.onFocus($event);
      });      
      this.editor.root.addEventListener("blur", ($event) => {
        this.onBlur($event);
      });  
    }
  }

  public get displayvalue(): any {
    return null;
  }
  
  public set displayvalue(val: any) {
   
  }
  
  public onKeydown(event: any) {
    if(event.keyCode == 27) {
      this.cancelEditing();
    } 
  }

  public startEditing() {
    console.log("QL Start editing");
    super.startEditing();
    this.editedValue = this.value;
  }

  public finishEditing() {
    console.log("QL Finish editing");
    if(this.mode == 'editor') {
      this.editedValue = (this.editor != null && this.editor.root != null ? this.editor.root.innerHTML : null);
    } else {
      this.editedValue = HtmlParser.stringify(HtmlParser.parse(this.codeSource));
    }
    this.commit(this.editedValue);
    super.finishEditing();
  }

  public toggleMode() {
    if(this.allowswitch) {
      this.mode = (this.mode == 'editor' ? 'code' : 'editor');
    }
  }

}


