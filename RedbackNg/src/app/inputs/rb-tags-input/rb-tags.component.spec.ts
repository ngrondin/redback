import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbTagsComponent } from './rb-tags.component';

describe('RbTagsComponent', () => {
  let component: RbTagsComponent;
  let fixture: ComponentFixture<RbTagsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbTagsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbTagsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
