import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbListItemComponent } from './rb-list-item.component';

describe('RbListItemComponent', () => {
  let component: RbListItemComponent;
  let fixture: ComponentFixture<RbListItemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbListItemComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbListItemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
