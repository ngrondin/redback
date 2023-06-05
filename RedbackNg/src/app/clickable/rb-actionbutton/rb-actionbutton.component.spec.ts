import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbActionButtonComponent } from './rb-actionbutton.component';

describe('RbButtonComponent', () => {
  let component: RbActionButtonComponent;
  let fixture: ComponentFixture<RbActionButtonComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbActionButtonComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbActionButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
