import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbPopupDatetimeComponent } from './rb-popup-datetime.component';

describe('RbPopupDatetimeComponent', () => {
  let component: RbPopupDatetimeComponent;
  let fixture: ComponentFixture<RbPopupDatetimeComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbPopupDatetimeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbPopupDatetimeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
