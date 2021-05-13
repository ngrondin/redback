import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { RbTileComponent } from './rb-tile.component';

describe('RbTileComponent', () => {
  let component: RbTileComponent;
  let fixture: ComponentFixture<RbTileComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ RbTileComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RbTileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
