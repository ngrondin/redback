import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbPopupComponent } from './rb-popup.component';

describe('RbPopupComponent', () => {
  let component: RbPopupComponent;
  let fixture: ComponentFixture<RbPopupComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbPopupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
