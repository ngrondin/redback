import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbSwitchInputComponent } from './rb-switch-input.component';

describe('RbSwitchInputComponent', () => {
  let component: RbSwitchInputComponent;
  let fixture: ComponentFixture<RbSwitchInputComponent>;

  beforeEach(async(() => {
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
