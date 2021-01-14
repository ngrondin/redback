import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbFormComponent } from './rb-form.component';

describe('RbFormComponent', () => {
  let component: RbFormComponent;
  let fixture: ComponentFixture<RbFormComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbFormComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
