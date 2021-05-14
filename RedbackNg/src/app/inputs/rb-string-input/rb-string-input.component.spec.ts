import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbStringInputComponent } from './rb-string-input.component';

describe('RbStringInputComponent', () => {
  let component: RbStringInputComponent;
  let fixture: ComponentFixture<RbStringInputComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbStringInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbStringInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
