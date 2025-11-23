import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbIconComponent } from './rb-icon.component';

describe('RbIconComponent', () => {
  let component: RbIconComponent;
  let fixture: ComponentFixture<RbIconComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbIconComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
