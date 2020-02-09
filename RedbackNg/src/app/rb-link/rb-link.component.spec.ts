import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbLinkComponent } from './rb-link.component';

describe('RbLinkComponent', () => {
  let component: RbLinkComponent;
  let fixture: ComponentFixture<RbLinkComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbLinkComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
