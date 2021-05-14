import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbSwitchInputComponent } from './rb-switch-input.component';

describe('RbSwitchInputComponent', () => {
  let component: RbSwitchInputComponent;
  let fixture: ComponentFixture<RbSwitchInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbSwitchInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbSwitchInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
