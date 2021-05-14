import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbLayoutComponent } from './rb-layout.component';

describe('RbLayoutComponent', () => {
  let component: RbLayoutComponent;
  let fixture: ComponentFixture<RbLayoutComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbLayoutComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbLayoutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
