import { Directive, ElementRef, Input, OnInit } from '@angular/core';
import { Headers, Http, Response } from '@angular/http';

@Directive({
  selector: 'rb-view-loader-dir'
})
export class RbViewLoaderDirective {

  @Input('src') private templateUrl: string;

  constructor(
    private element: ElementRef, 
    private http: Http) {}

  parseTemplate(res: Response) {
    this.element.nativeElement.innerHTML = res.text();
  }  

  ngOnInit() {
    this.http.get(this.templateUrl, { withCredentials: true }).subscribe(res => this.parseTemplate(res))
  }
}
