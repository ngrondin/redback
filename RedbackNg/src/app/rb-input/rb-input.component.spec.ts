import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbInputComponent } from './rb-input.component';

describe('RbInputComponent', () => {
  let component: RbInputComponent;
  let fixture: ComponentFixture<RbInputComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbInputComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
