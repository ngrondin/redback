import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbMenuComponent } from './rb-menu.component';

describe('RbMenuComponent', () => {
  let component: RbMenuComponent;
  let fixture: ComponentFixture<RbMenuComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbMenuComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
