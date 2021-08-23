import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbNotificationBellComponent } from './rb-notification-bell.component';

describe('RbNotificationBellComponent', () => {
  let component: RbNotificationBellComponent;
  let fixture: ComponentFixture<RbNotificationBellComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbNotificationBellComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RbNotificationBellComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
