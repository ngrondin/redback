import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RbHcollapseComponent } from './rb-hcollapse.component';

describe('RbHcollapseComponent', () => {
  let component: RbHcollapseComponent;
  let fixture: ComponentFixture<RbHcollapseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RbHcollapseComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RbHcollapseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
