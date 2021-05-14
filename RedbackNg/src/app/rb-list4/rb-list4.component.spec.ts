import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbList4Component } from './rb-list4.component';

describe('RbList4Component', () => {
  let component: RbList4Component;
  let fixture: ComponentFixture<RbList4Component>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbList4Component ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbList4Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
