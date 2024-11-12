import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbImageComponent } from './rb-image.component';

describe('RbImageComponent', () => {
  let component: RbImageComponent;
  let fixture: ComponentFixture<RbImageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbImageComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbImageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
