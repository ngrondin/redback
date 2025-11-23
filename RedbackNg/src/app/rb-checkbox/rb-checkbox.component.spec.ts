import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbCheckboxComponent } from './rb-checkbox.component';

describe('RbCheckboxComponent', () => {
  let component: RbCheckboxComponent;
  let fixture: ComponentFixture<RbCheckboxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbCheckboxComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbCheckboxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
