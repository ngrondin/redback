import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbIconbuttonComponent } from './rb-iconbutton.component';

describe('RbIconbuttonComponent', () => {
  let component: RbIconbuttonComponent;
  let fixture: ComponentFixture<RbIconbuttonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbIconbuttonComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbIconbuttonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
