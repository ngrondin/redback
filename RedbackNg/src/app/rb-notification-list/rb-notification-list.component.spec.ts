import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbNotificationListComponent } from './rb-notification-list.component';

describe('RbNotificationListComponent', () => {
  let component: RbNotificationListComponent;
  let fixture: ComponentFixture<RbNotificationListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbNotificationListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RbNotificationListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
