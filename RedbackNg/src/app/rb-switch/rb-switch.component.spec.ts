import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbSwitchComponent } from './rb-switch.component';

describe('RbSwitchComponent', () => {
  let component: RbSwitchComponent;
  let fixture: ComponentFixture<RbSwitchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbSwitchComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbSwitchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
