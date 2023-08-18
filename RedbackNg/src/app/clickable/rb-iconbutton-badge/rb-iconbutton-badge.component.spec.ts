import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbIconbuttonBadgeComponent } from './rb-iconbutton-badge.component';

describe('RbIconbuttonBadgeComponent', () => {
  let component: RbIconbuttonBadgeComponent;
  let fixture: ComponentFixture<RbIconbuttonBadgeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbIconbuttonBadgeComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbIconbuttonBadgeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
