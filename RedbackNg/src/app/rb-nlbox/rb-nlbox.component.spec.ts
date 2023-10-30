import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbNlboxComponent } from './rb-nlbox.component';

describe('RbNlboxComponent', () => {
  let component: RbNlboxComponent;
  let fixture: ComponentFixture<RbNlboxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbNlboxComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbNlboxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
