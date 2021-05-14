import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbTextareaInputComponent } from './rb-textarea-input.component';

describe('RbTextareaInputComponent', () => {
  let component: RbTextareaInputComponent;
  let fixture: ComponentFixture<RbTextareaInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbTextareaInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbTextareaInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
