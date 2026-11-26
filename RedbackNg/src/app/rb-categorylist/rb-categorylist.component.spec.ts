import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { Subject } from 'rxjs';

import { RbCategorylistComponent } from './rb-categorylist.component';

describe('RbCategorylistComponent', () => {
  let component: RbCategorylistComponent;
  let fixture: ComponentFixture<RbCategorylistComponent>;

  beforeEach(waitForAsync(() => {
    window.redback = { getObservable: () => new Subject() };
    TestBed.configureTestingModule({
      declarations: [ RbCategorylistComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbCategorylistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
