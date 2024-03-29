import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbViewLoaderComponent } from './rb-view-loader.component';

describe('RbViewLoaderComponent', () => {
  let component: RbViewLoaderComponent;
  let fixture: ComponentFixture<RbViewLoaderComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbViewLoaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbViewLoaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
