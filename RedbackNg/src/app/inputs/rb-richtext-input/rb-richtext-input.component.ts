import { Component, Input, ViewChild } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';
import Quill from 'quill'
import { HtmlParser } from 'app/helpers';

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

  ngAfterViewInit() {
    this.editor = new Quill('#quilleditor', this.editorconfig);
    this.editor.root.addEventListener("focus", ($event) => {
      this.onFocus($event);
    });      
    this.editor.root.addEventListener("blur", ($event) => {
      this.onBlur($event);
    });
  }


  onDatasetEvent(event: string) {
    if(this.innerHtml != this.value) this.innerHtml = this.value;
    if(this.codeSource != this.value) this.codeSource = HtmlParser.stringify(HtmlParser.parse(this.value), true);
    if(this.editor != null) this.editor.enable(!this.readonly);
  }

  public get displayvalue(): any {
    return null;
  }
  
  public set displayvalue(val: any) {
   
  }

  public get innerHtml() : string {
    return this.editor != null && this.editor.root != null ? this.editor.root.innerHTML : null;
  }

  public set innerHtml(val: string) {
    if(this.editor != null && this.editor.root != null) {
      this.editor.root.innerHTML = val;  
    }
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
      this.editedValue = this.innerHtml;
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


