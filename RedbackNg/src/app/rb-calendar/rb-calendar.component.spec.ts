import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbCalendarComponent } from './rb-calendar.component';

describe('RbCalendarComponent', () => {
  let component: RbCalendarComponent;
  let fixture: ComponentFixture<RbCalendarComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbCalendarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
