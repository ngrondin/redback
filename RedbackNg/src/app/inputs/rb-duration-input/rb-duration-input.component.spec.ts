import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbDurationInputComponent } from './rb-duration-input.component';

describe('RbDurationInputComponent', () => {
  let component: RbDurationInputComponent;
  let fixture: ComponentFixture<RbDurationInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbDurationInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbDurationInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
