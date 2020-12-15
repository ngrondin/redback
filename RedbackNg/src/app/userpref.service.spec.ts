import { TestBed } from '@angular/core/testing';

import { UserprefService } from './userpref.service';

describe('UserprefService', () => {
  let service: UserprefService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(UserprefService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
