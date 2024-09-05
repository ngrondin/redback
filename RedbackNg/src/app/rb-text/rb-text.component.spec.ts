import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbTextComponent } from './rb-text.component';

describe('RbTextComponent', () => {
  let component: RbTextComponent;
  let fixture: ComponentFixture<RbTextComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbTextComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbTextComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
