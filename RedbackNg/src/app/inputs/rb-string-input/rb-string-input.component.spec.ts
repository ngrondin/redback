import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbStringInputComponent } from './rb-string-input.component';

describe('RbStringInputComponent', () => {
  let component: RbStringInputComponent;
  let fixture: ComponentFixture<RbStringInputComponent>;

  beforeEach(async(() => {
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
