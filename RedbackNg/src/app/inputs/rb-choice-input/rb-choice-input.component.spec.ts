import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbChoiceInputComponent } from './rb-choice-input.component';

describe('RbChoiceInputComponent', () => {
  let component: RbChoiceInputComponent;
  let fixture: ComponentFixture<RbChoiceInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbChoiceInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbChoiceInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
