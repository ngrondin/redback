import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { RedbackModule } from './redback.module';
import { MatIconModule } from '@angular/material';
import { HttpClientModule } from '@angular/common/http';


@NgModule({
  imports: [
    BrowserModule,
    MatIconModule,
    RedbackModule,
    HttpClientModule
  ],
  exports: [
    MatIconModule
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
