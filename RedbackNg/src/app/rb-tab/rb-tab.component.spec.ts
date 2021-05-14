import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbTabComponent } from './rb-tab.component';

describe('RbTabComponent', () => {
  let component: RbTabComponent;
  let fixture: ComponentFixture<RbTabComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
