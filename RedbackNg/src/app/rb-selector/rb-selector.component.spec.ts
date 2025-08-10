import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbSelectorComponent } from './rb-selector.component';

describe('RbSelectorComponent', () => {
  let component: RbSelectorComponent;
  let fixture: ComponentFixture<RbSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbSelectorComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
