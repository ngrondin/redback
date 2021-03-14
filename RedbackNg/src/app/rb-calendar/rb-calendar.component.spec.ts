import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbCalendarComponent } from './rb-calendar.component';

describe('RbCalendarComponent', () => {
  let component: RbCalendarComponent;
  let fixture: ComponentFixture<RbCalendarComponent>;

  beforeEach(async(() => {
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
