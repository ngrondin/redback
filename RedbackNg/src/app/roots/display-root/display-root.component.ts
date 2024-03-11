import { Component, ViewChild } from '@angular/core';
import { AppRootComponent } from '../abstract/app-root';
import { MatSidenav } from '@angular/material/sidenav';
import { RbViewLoaderComponent } from 'app/rb-view-loader/rb-view-loader.component';

@Component({
  selector: 'display-root',
  templateUrl: './display-root.component.html',
  styleUrls: ['./display-root.component.css']
})
export class DisplayRootComponent extends AppRootComponent {
  @ViewChild("viewloader") viewloader: RbViewLoaderComponent;
  
  constructor(
    ) {
      super();
    }
  
    ngOnInit() {
      super.ngOnInit();
      setInterval(() => {
        this.viewloader.currentLoadedView.forceRefresh();
      }, 300000)
    }

}
