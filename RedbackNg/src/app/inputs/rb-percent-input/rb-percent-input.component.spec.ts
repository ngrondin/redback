import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbPercentInputComponent } from './rb-percent-input.component';

describe('RbPercentInputComponent', () => {
  let component: RbPercentInputComponent;
  let fixture: ComponentFixture<RbPercentInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbPercentInputComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RbPercentInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
