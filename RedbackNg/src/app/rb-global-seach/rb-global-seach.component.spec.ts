import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbGlobalSeachComponent } from './rb-global-seach.component';

describe('RbGlobalSeachComponent', () => {
  let component: RbGlobalSeachComponent;
  let fixture: ComponentFixture<RbGlobalSeachComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbGlobalSeachComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbGlobalSeachComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
