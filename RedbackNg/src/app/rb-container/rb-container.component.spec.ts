import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbContainerComponent } from './rb-container.component';

describe('RbContainerComponent', () => {
  let component: RbContainerComponent;
  let fixture: ComponentFixture<RbContainerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbContainerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
