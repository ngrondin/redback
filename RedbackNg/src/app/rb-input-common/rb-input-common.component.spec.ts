import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbInputCommonComponent } from './rb-input-common.component';

describe('RbInputCommonComponent', () => {
  let component: RbInputCommonComponent;
  let fixture: ComponentFixture<RbInputCommonComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbInputCommonComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbInputCommonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
