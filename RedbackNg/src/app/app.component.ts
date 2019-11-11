import { Component, Input, ElementRef } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title: string; 
  type: string;
  initialView: string;

  constructor(public elementRef: ElementRef) {
    var native = this.elementRef.nativeElement;
    this.type = native.getAttribute("type");
    this.title = native.getAttribute("title");
    this.initialView = native.getAttribute("initialView");
  }
}
