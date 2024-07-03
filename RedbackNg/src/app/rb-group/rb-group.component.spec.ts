import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbGroupComponent } from './rb-group.component';

describe('RbGroupComponent', () => {
  let component: RbGroupComponent;
  let fixture: ComponentFixture<RbGroupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbGroupComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbGroupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
