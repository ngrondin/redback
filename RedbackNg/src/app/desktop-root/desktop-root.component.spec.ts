import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { DesktopRootComponent } from './desktop-root.component';

describe('DesktopRootComponent', () => {
  let component: DesktopRootComponent;
  let fixture: ComponentFixture<DesktopRootComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ DesktopRootComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DesktopRootComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
