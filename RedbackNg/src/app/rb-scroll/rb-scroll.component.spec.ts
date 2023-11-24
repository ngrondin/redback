import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbScrollComponent } from './rb-scroll.component';

describe('RbScrollComponent', () => {
  let component: RbScrollComponent;
  let fixture: ComponentFixture<RbScrollComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbScrollComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbScrollComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
