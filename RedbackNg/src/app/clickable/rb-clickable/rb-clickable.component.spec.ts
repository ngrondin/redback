import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbClickableComponent } from './rb-clickable.component';

describe('ClickableComponent', () => {
  let component: RbClickableComponent;
  let fixture: ComponentFixture<RbClickableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbClickableComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbClickableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
