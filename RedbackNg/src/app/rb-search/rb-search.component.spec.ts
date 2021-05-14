import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbSearchComponent } from './rb-search.component';

describe('RbSearchComponent', () => {
  let component: RbSearchComponent;
  let fixture: ComponentFixture<RbSearchComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbSearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
