import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbGlobalSearchResultComponent } from './rb-popup-globalsearch.component';

describe('RbGlobalSearchResultComponent', () => {
  let component: RbGlobalSearchResultComponent;
  let fixture: ComponentFixture<RbGlobalSearchResultComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbGlobalSearchResultComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbGlobalSearchResultComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
