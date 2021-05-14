import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbMenuGroupComponent } from './rb-menu-group.component';

describe('RbMenuGroupComponent', () => {
  let component: RbMenuGroupComponent;
  let fixture: ComponentFixture<RbMenuGroupComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbMenuGroupComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbMenuGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
