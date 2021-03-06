import { Component, Input, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { MatIconRegistry } from '@angular/material';
import { DomSanitizer } from '@angular/platform-browser';
import { ApiService } from './services/api.service';
import { ConfigService } from './services/config.service';
import { UserprefService } from './services/userpref.service';

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
      private configService: ConfigService,
      private userprefService: UserprefService ) {
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
    this.apiService.domainService = native.getAttribute("domainservice");
    this.apiService.reportService = native.getAttribute("reportservice");
    this.apiService.processService = native.getAttribute("processservice");
    this.apiService.userprefService = native.getAttribute("userpreferenceservice");
    this.apiService.signalService = native.getAttribute("signalservice");
    this.apiService.chatService = native.getAttribute("chatservice");
    let objectsString: string = native.getAttribute("objects");
    if(objectsString.length > 0) {
      this.configService.setObjectsConfig(JSON.parse(objectsString.replace(/'/g, '"')));
    } 

    let currentUrl = window.location.href;
    let pos = currentUrl.indexOf(this.apiService.uiService);
    if(pos > -1) {
      currentUrl = currentUrl.substring(0, pos - 1);
    }
    if(currentUrl.endsWith("#")) {
      currentUrl = currentUrl.substring(0, currentUrl.length - 1);
    }
    if(currentUrl.endsWith("/")) {
      currentUrl = currentUrl.substring(0, currentUrl.length - 1);
    }
    this.apiService.baseUrl = currentUrl;

    this.iconsets = JSON.parse(native.getAttribute("iconsets").replace(/'/g, '"'))
    for(const set of this.iconsets) {
      this.matIconRegistry.addSvgIconSetInNamespace(set, this.domSanitizer.bypassSecurityTrustResourceUrl(this.apiService.baseUrl + '/' + this.apiService.uiService + '/resource/' + set + '.svg'));
    }
    this.userprefService.load();
  }
}
