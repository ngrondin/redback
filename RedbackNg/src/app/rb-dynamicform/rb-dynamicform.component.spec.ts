import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbDynamicformComponent } from './rb-dynamicform.component';

describe('RbDynamicformComponent', () => {
  let component: RbDynamicformComponent;
  let fixture: ComponentFixture<RbDynamicformComponent>;

  beforeEach(async(() => {
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
