import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbUsermenuComponent } from './rb-usermenu.component';

describe('RbUsermenuComponent', () => {
  let component: RbUsermenuComponent;
  let fixture: ComponentFixture<RbUsermenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbUsermenuComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbUsermenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
