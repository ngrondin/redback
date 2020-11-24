import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbHsectionComponent } from './rb-hsection.component';

describe('RbHsectionComponent', () => {
  let component: RbHsectionComponent;
  let fixture: ComponentFixture<RbHsectionComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbHsectionComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbHsectionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
