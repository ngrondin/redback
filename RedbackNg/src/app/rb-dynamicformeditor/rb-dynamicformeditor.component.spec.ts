import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbDynamicformeditorComponent } from './rb-dynamicformeditor.component';

describe('RbDynamicformeditorComponent', () => {
  let component: RbDynamicformeditorComponent;
  let fixture: ComponentFixture<RbDynamicformeditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbDynamicformeditorComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbDynamicformeditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
