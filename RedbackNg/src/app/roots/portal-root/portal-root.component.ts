import { Component, ViewChild } from '@angular/core';
import { AppRootComponent } from '../abstract/app-root';
import { MatSidenav } from '@angular/material/sidenav';

@Component({
  selector: 'portal-root',
  templateUrl: './portal-root.component.html',
  styleUrls: ['./portal-root.component.css']
})
export class PortalRootComponent extends AppRootComponent {
  @ViewChild("rightdrawer") rightdrawer: MatSidenav;
  
  constructor(
    ) {
      super();
    }
  


}
