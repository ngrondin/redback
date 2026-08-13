import { TestBed } from '@angular/core/testing';

import { CollabService } from './collab.service';

describe('CollabService', () => {
  let service: CollabService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CollabService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
