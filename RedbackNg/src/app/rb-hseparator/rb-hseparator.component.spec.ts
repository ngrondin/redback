import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbHseparatorComponent } from './rb-hseparator.component';

describe('RbHseparatorComponent', () => {
  let component: RbHseparatorComponent;
  let fixture: ComponentFixture<RbHseparatorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbHseparatorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbHseparatorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
