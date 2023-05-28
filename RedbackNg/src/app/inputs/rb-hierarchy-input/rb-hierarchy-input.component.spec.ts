import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbHierarchyInputComponent } from './rb-hierarchy-input.component';

describe('RbHierarchyInputComponent', () => {
  let component: RbHierarchyInputComponent;
  let fixture: ComponentFixture<RbHierarchyInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbHierarchyInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbHierarchyInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
