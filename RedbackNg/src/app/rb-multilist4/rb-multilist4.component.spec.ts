import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbMultilist4Component } from './rb-multilist4.component';

describe('RbMultilist4Component', () => {
  let component: RbMultilist4Component;
  let fixture: ComponentFixture<RbMultilist4Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbMultilist4Component ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbMultilist4Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
