import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbBreadcrumbComponent } from './rb-breadcrumb.component';

describe('RbBreadcrumbComponent', () => {
  let component: RbBreadcrumbComponent;
  let fixture: ComponentFixture<RbBreadcrumbComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbBreadcrumbComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbBreadcrumbComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
