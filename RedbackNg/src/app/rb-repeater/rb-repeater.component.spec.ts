import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbRepeaterComponent } from './rb-repeater.component';

describe('RbRepeaterComponent', () => {
  let component: RbRepeaterComponent;
  let fixture: ComponentFixture<RbRepeaterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbRepeaterComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbRepeaterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
