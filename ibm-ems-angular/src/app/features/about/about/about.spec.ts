import { TestBed } from '@angular/core/testing';

import { About } from './about';

describe('About', () => {
  let service: About;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(About);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
