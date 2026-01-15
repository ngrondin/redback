import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbAboutComponent } from './rb-about.component';

describe('RbAboutComponent', () => {
  let component: RbAboutComponent;
  let fixture: ComponentFixture<RbAboutComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbAboutComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbAboutComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
