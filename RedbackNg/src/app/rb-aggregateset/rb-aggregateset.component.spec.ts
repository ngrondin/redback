import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import { RbAggregatesetComponent } from './rb-aggregateset.component';

describe('RbAggregatesetComponent', () => {
  let component: RbAggregatesetComponent;
  let fixture: ComponentFixture<RbAggregatesetComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ RbAggregatesetComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbAggregatesetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
