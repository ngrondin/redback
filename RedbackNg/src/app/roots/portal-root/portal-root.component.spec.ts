import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PortalRootComponent } from './portal-root.component';

describe('PortalRootComponent', () => {
  let component: PortalRootComponent;
  let fixture: ComponentFixture<PortalRootComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PortalRootComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PortalRootComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
