import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { RedbackModule } from './redback.module';
import { MatIconModule } from '@angular/material';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ToastrModule } from 'ngx-toastr';

import {Compiler, COMPILER_OPTIONS, CompilerFactory} from '@angular/core';
import {JitCompilerFactory} from '@angular/platform-browser-dynamic';
export function createCompiler(compilerFactory: CompilerFactory) {
  return compilerFactory.createCompiler();
}

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
    {provide: COMPILER_OPTIONS, useValue: {}, multi: true},
    {provide: CompilerFactory, useClass: JitCompilerFactory, deps: [COMPILER_OPTIONS]},
    {provide: Compiler, useFactory: createCompiler, deps: [CompilerFactory]}    
  ],
  entryComponents :[
  ],
  bootstrap: [
    AppComponent
  ]
})
export class AppModule { }
