import { Component, Input, ElementRef, OnInit, ViewChild, ViewContainerRef } from '@angular/core';
import { MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { ApiService } from './services/api.service';
import { ConfigService } from './services/config.service';
import { UserprefService } from './services/userpref.service';
import { NotificationService } from './services/notification.service';
import { ClientWSService } from './services/clientws.service';
import { MenuService } from './services/menu.service';
import { Observable, Subject } from 'rxjs';
import { DataService } from './services/data.service';
import { ModalService } from './services/modal.service';
import { ChatService } from './services/chat.service';
import { SecurityService } from './services/security.service';
import { LogService } from './services/log.service';

@Component({
  viewProviders: [MatIconRegistry],
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  appname: string;
  version: string;
  username: string;
  userdisplay: string;

  apptitle: string; 
  logo: string;
  layout: string;
  initialView: string;
  initialViewTitle: string;
  menuView: string;
  iconsets: string[];

  onloadFunction: any;

  events: Subject<string> = new Subject<string>();
  firstConnected: boolean = false;
  appConfigLoadTries: number = 0;
  

  constructor(
      private elementRef: ElementRef,
      private matIconRegistry: MatIconRegistry,
      private domSanitizer: DomSanitizer,
      private securityService: SecurityService,
      private apiService: ApiService,
      private dataService: DataService,
      private clientWSService: ClientWSService,
      private configService: ConfigService,
      private notificationService: NotificationService,
      private userprefService: UserprefService,
      private menuService: MenuService,
      private modalService: ModalService,
      private chatService: ChatService,
      private logService: LogService
   ) {
    var native = this.elementRef.nativeElement;

    this.appname = native.getAttribute("name");
    this.version = native.getAttribute("version");
    this.username = native.getAttribute("username");
    this.userdisplay = native.getAttribute("userdisplay");

    this.apiService.uiService = native.getAttribute("uiservice");
    this.apiService.objectService = native.getAttribute("objectservice");
    this.apiService.fileService = native.getAttribute("fileservice");
    this.apiService.reportService = native.getAttribute("reportservice");
    this.apiService.processService = native.getAttribute("processservice");
    this.apiService.userprefService = native.getAttribute("userpreferenceservice");
    this.apiService.chatService = native.getAttribute("chatservice");
    this.apiService.aiService = native.getAttribute("aiservice");
    this.apiService.useCSForAPI = native.getAttribute("usecsforapi") == "true" ? true : false;
    this.clientWSService.path = native.getAttribute("clientservice");
    this.userprefService.userdisplay = this.userdisplay;
    this.userprefService.username = this.username;

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
    this.securityService.baseUrl = currentUrl;
    this.clientWSService.baseUrl = currentUrl;
    window.redback.username = this.username;
    window.redback.api = this.apiService;
    window.redback.data = this.dataService;
    window.redback.modal = this.modalService;
    window.redback.notifications = this.notificationService;
    window.redback.observers = [];
    window.redback.getObservable = () => {
      return new Observable<string>((observer) => {
        window.redback.observers.push(observer);
      });
    }
    window.redback.publishEvent = (event: string)=> {
      window.redback.observers.forEach((observer) => {
        observer.next(event);
      }); 
    }
  }

  ngOnInit(): void {
    this.loadAppConfig();
  }  

  loadAppConfig() {
    this.apiService.getAppConfig(this.appname).subscribe({
      next: cfg => this.onAppConfig(cfg),
      error: err => {
        this.appConfigLoadTries++;
        if(this.appConfigLoadTries < 3) {
          setTimeout(this.loadAppConfig, 1000);
        }
      }
    });
  }

  onAppConfig(config: any) {
    this.appConfigLoadTries = 0;
    this.layout = config['layout'];
    this.apptitle = config['label'];
    this.logo = config['logo'];
    this.initialView = config['defaultview'];
    this.menuView = this.appname; //TODO: Fix this
    this.configService.setObjectsConfig(config['objects']);
    this.configService.setNLCommandModel(config["nlcommandmodel"]);
    this.configService.setNLCommandLabel(config["nlcommandlabel"]);
    this.configService.setChatLabel(config["chatlabel"]);
    this.configService.setPersonalViews(config["personalviews"]);
    this.iconsets = config["iconsets"];
    this.menuService.setFullMenuConfig(config['menu']);
    for(const set of (this.iconsets || [])) {
      this.matIconRegistry.addSvgIconSetInNamespace(set, this.domSanitizer.bypassSecurityTrustResourceUrl(this.apiService.baseUrl + '/' + this.apiService.uiService + '/resource/' + set + '.svg'), {viewBox: "0 0 24 24"});
    }
    let preferences = config["preferences"];
    if(preferences != null) {
      preferences.forEach(element => {
        this.userprefService.addGlobalPreference(element);
      });  
    }
    if(config['onload'] != null) {
      eval("this.onloadFunction = async function() {" + config['onload'] + "}"); // Didn't use Function as this needs to be async
    }

    this.clientWSService.initWebsocket().subscribe({
      next: connected => this.firstLoad()
    });
    setTimeout(() => this.firstLoad(), 5000); //If the client websocket doesn't connect in 5s, fallback on http
  }

  async firstLoad() {
    if(this.firstConnected == false) {
      this.firstConnected = true;
      await this.menuService.loadPreferences();
      await this.userprefService.load();
      this.logService.level = this.userprefService.getGlobalPreferenceValue("loglevel") ?? false;
      await this.notificationService.load();
      await this.chatService.load();
      if(this.onloadFunction != null) {
        await this.onloadFunction.call(window.redback);
      }
      this.logService.info("Initiation view load");
      this.events.next("init");
    }
  }
}
