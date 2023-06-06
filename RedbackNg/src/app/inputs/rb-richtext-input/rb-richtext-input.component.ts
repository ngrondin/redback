import { AfterViewInit, Component, ViewChild } from '@angular/core';
import { RbFieldInputComponent } from '../abstract/rb-field-input';
import { EditorChangeContent, EditorChangeSelection } from 'ngx-quill';
import Quill from 'quill'

@Component({
  selector: 'rb-richtext-input',
  templateUrl: './rb-richtext-input.component.html',
  styleUrls: ['./rb-richtext-input.component.css']
})
export class RbRichtextInputComponent extends RbFieldInputComponent {
  @ViewChild('quilleditorparent') quilleditorparent; 

  hackDone: boolean = false;

  public get displayvalue(): any {
    if(this.isEditing) {
      return this.editedValue;
    } else {
      return this.value;
    }
  }
  
  public set displayvalue(val: any) {
    if(this.isEditing) {
      this.editedValue = val;
    } 
  }

  created(event: Quill) {
  }

  changedEditor(event: EditorChangeContent | EditorChangeSelection) {
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
    if(!this.hackDone) {
      this.hackQuillEditor();
    }
  }

  public finishEditing() {
    console.log("QL Finish editing");
    this.commit(this.editedValue);
    super.finishEditing();
  }

  hackQuillEditor() {
    var qep = this.quilleditorparent.elementRef.nativeElement;
    var list = qep.getElementsByClassName('ql-editor');
    if(list.length > 0) {
      var qe = list[0];
      qe.addEventListener("focus", ($event) => {
        console.log('QL hack focus')
        this.onFocus($event);
      });      
      qe.addEventListener("blur", ($event) => {
        console.log('QL hack blur')
        this.onBlur($event);
      });
    }
    this.hackDone = true;
    console.log("Hack done");
  }
}


