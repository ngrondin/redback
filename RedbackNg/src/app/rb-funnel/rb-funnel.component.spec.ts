import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbFunnelComponent } from './rb-funnel.component';

describe('RbFunnelComponent', () => {
  let component: RbFunnelComponent;
  let fixture: ComponentFixture<RbFunnelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbFunnelComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbFunnelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
