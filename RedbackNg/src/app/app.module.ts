import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { RedbackModule } from './redback.module';
import { MatIconModule } from '@angular/material';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';


@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    MatIconModule,
    RedbackModule,
    HttpClientModule,
    ToastrModule.forRoot()
  ],
  exports: [
    MatIconModule
  ],
  declarations: [
    AppComponent 
  ],
  providers: [
  ],
  entryComponents :[
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
