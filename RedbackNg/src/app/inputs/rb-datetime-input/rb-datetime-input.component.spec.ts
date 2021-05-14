import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbDatetimeInputComponent } from './rb-datetime-input.component';

describe('RbDatetimeInputComponent', () => {
  let component: RbDatetimeInputComponent;
  let fixture: ComponentFixture<RbDatetimeInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbDatetimeInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbDatetimeInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
