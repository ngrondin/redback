import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbButtonComponent } from './rb-button.component';

describe('RbButtonComponent', () => {
  let component: RbButtonComponent;
  let fixture: ComponentFixture<RbButtonComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbButtonComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
