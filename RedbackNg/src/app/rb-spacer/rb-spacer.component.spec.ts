import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbSpacerComponent } from './rb-spacer.component';

describe('RbSpacerComponent', () => {
  let component: RbSpacerComponent;
  let fixture: ComponentFixture<RbSpacerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbSpacerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbSpacerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
