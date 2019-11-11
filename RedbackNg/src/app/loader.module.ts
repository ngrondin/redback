import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RbViewLoaderComponent } from './rb-view-loader/rb-view-loader.component';
import { RedbackModule } from './redback.module';



@NgModule({
  imports: [
    RedbackModule
  ],
  declarations: [
    RbViewLoaderComponent
  ],
  exports: [
    RbViewLoaderComponent
  ]
})
export class ViewLoaderModule {
  static forRoot()
  {
      return {
          ngModule: ViewLoaderModule,
          providers: [ ], 
      };
  }
}
