import { Component, OnInit, Input } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';
import { ApiService } from 'app/api.service';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'desktop-root',
  templateUrl: './desktop-root.component.html',
  styleUrls: ['./desktop-root.component.css']
})
export class DesktopRootComponent implements OnInit {

  @Input() title : string;
  @Input() logo : string;
  @Input() username : string;
  @Input() userdisplay : string;
  @Input() initialView : string;
  @Input() initialViewTitle : string;
  @Input() menuView : string;
  @Input() version : string;
  view: string;
  viewTitle: string;
 
  constructor(
    private cookieService : CookieService,
    private apiService : ApiService,
    private domSanitizer: DomSanitizer
  ) { }

  ngOnInit() {
    //this.cookieService.set('rbtoken','eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJlbWFpbCI6Im5ncm9uZGluNzhAZ21haWwuY29tIiwiZXhwIjoxOTIwNTExOTIyMDAwfQ.zQrN7sheh1PuO4fWru45dTPDtkLAqB9Q0WrwGO6yOeo', 1920511922000, "/", "http://localhost", false, 'Lax');
    if(this.version == null)
      this.version = 'default';
    this.view = this.initialView;
    this.viewTitle = this.initialViewTitle;
  }

  get viewUrl() : string {
    return this.apiService.baseUrl + '/' + this.apiService.uiService + '/view/' + this.version + '/' + this.view;
  }

  get menuUrl() : string {
    return this.apiService.baseUrl + '/' + this.apiService.uiService + '/menu/' + this.version + '/' + this.menuView;
  }

  get logoUrl() : any {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(this.logo);
  }

  navigateTo($event) {
    this.view = $event.view;
    this.viewTitle = $event.title;
  }

}
