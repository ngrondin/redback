import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbDynamicformComponent } from './rb-dynamicform.component';

describe('RbDynamicformComponent', () => {
  let component: RbDynamicformComponent;
  let fixture: ComponentFixture<RbDynamicformComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbDynamicformComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbDynamicformComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
