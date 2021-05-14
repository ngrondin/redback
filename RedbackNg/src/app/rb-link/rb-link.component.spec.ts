import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbLinkComponent } from './rb-link.component';

describe('RbLinkComponent', () => {
  let component: RbLinkComponent;
  let fixture: ComponentFixture<RbLinkComponent>;

  beforeEach(waitForAsync(() => {
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
