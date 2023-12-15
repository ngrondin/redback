import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbLinktableComponent } from './rb-linktable.component';

describe('RbLinktableComponent', () => {
  let component: RbLinktableComponent;
  let fixture: ComponentFixture<RbLinktableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbLinktableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbLinktableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
