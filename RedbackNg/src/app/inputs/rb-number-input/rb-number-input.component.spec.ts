import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbNumberInputComponent } from './rb-number-input.component';

describe('RbNumberInputComponent', () => {
  let component: RbNumberInputComponent;
  let fixture: ComponentFixture<RbNumberInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbNumberInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbNumberInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
