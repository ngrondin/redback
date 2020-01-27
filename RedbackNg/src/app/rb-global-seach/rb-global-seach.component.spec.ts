import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbGlobalSeachComponent } from './rb-global-seach.component';

describe('RbGlobalSeachComponent', () => {
  let component: RbGlobalSeachComponent;
  let fixture: ComponentFixture<RbGlobalSeachComponent>;

  beforeEach(async(() => {
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
