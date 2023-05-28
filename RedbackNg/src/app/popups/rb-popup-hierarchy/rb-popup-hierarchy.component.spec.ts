import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbPopupHierarchyComponent } from './rb-popup-hierarchy.component';

describe('RbPopupHierarchyComponent', () => {
  let component: RbPopupHierarchyComponent;
  let fixture: ComponentFixture<RbPopupHierarchyComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbPopupHierarchyComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbPopupHierarchyComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
