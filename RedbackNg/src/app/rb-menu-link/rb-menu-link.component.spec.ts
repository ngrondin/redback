import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbMenuLinkComponent } from './rb-menu-link.component';

describe('RbMenuLinkComponent', () => {
  let component: RbMenuLinkComponent;
  let fixture: ComponentFixture<RbMenuLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbMenuLinkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbMenuLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
