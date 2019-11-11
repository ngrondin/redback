import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { RedbackModule } from './redback.module';
import { ViewLoaderModule } from './loader.module';
import { RbViewLoaderComponent } from './rb-view-loader/rb-view-loader.component';
import { CommonModule } from '@angular/common';


@NgModule({
  imports: [
    BrowserModule,
    RedbackModule
  ],
  declarations: [
    AppComponent
  ],
  providers: [
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
