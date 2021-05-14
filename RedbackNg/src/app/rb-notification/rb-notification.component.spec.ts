import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbNotificationComponent } from './rb-notification.component';

describe('RbNotificationComponent', () => {
  let component: RbNotificationComponent;
  let fixture: ComponentFixture<RbNotificationComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbNotificationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
