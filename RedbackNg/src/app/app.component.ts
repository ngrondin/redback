import { Component, Input, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { MatIconRegistry } from '@angular/material';
import { DomSanitizer } from '@angular/platform-browser';
import { ApiService } from './api.service';
import { ConfigService } from './config.service';

@Component({
  viewProviders: [MatIconRegistry],
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  apptitle: string; 
  logo: string;
  type: string;
  version: string;
  username: string;
  userdisplay: string;
  initialView: string;
  initialViewTitle: string;
  menuView: string;
  iconsets: string[];
  

  constructor(
      private elementRef: ElementRef,
      private matIconRegistry: MatIconRegistry,
      private domSanitizer: DomSanitizer,
      private apiService: ApiService,
      private configService: ConfigService ) {
    var native = this.elementRef.nativeElement;
    this.type = native.getAttribute("type");
    this.apptitle = native.getAttribute("apptitle");
    this.logo = native.getAttribute("logo");
    this.version = native.getAttribute("version");
    this.username = native.getAttribute("username");
    this.userdisplay = native.getAttribute("userdisplay");
    this.initialView = native.getAttribute("initialview");
    this.initialViewTitle = native.getAttribute("initialviewtitle");
    this.menuView = native.getAttribute("menuview");
    this.apiService.uiService = native.getAttribute("uiservice");
    this.apiService.objectService = native.getAttribute("objectservice");
    this.apiService.fileService = native.getAttribute("fileservice");
    this.apiService.processService = native.getAttribute("processservice");
    this.apiService.signalService = native.getAttribute("signalservice");
    let objectsString: string = native.getAttribute("objects");
    if(objectsString.length > 0) {
      this.configService.setObjectsConfig(JSON.parse(objectsString.replace(/'/g, '"')));
    } 

    let currentUrl = window.location.href;
    let pos = currentUrl.indexOf(this.apiService.uiService);
    this.apiService.baseUrl = currentUrl.substring(0, pos - 1);

    this.iconsets = native.getAttribute("iconsets").split(",");
    for(const set of this.iconsets) {
      this.matIconRegistry.addSvgIconSetInNamespace(set, this.domSanitizer.bypassSecurityTrustResourceUrl(this.apiService.baseUrl + '/' + this.apiService.uiService + '/resource/' + set + '.svg'));
    }
  }
}
