import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IEventBalance } from '../event-balance.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../event-balance.test-samples';

import { EventBalanceService } from './event-balance.service';

const requireRestSample: IEventBalance = {
  ...sampleWithRequiredData,
};

describe('EventBalance Service', () => {
  let service: EventBalanceService;
  let httpMock: HttpTestingController;
  let expectedResult: IEventBalance | IEventBalance[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(EventBalanceService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a EventBalance', () => {
      const eventBalance = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(eventBalance).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a EventBalance', () => {
      const eventBalance = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(eventBalance).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a EventBalance', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of EventBalance', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a EventBalance', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addEventBalanceToCollectionIfMissing', () => {
      it('should add a EventBalance to an empty array', () => {
        const eventBalance: IEventBalance = sampleWithRequiredData;
        expectedResult = service.addEventBalanceToCollectionIfMissing([], eventBalance);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(eventBalance);
      });

      it('should not add a EventBalance to an array that contains it', () => {
        const eventBalance: IEventBalance = sampleWithRequiredData;
        const eventBalanceCollection: IEventBalance[] = [
          {
            ...eventBalance,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addEventBalanceToCollectionIfMissing(eventBalanceCollection, eventBalance);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a EventBalance to an array that doesn't contain it", () => {
        const eventBalance: IEventBalance = sampleWithRequiredData;
        const eventBalanceCollection: IEventBalance[] = [sampleWithPartialData];
        expectedResult = service.addEventBalanceToCollectionIfMissing(eventBalanceCollection, eventBalance);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(eventBalance);
      });

      it('should add only unique EventBalance to an array', () => {
        const eventBalanceArray: IEventBalance[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const eventBalanceCollection: IEventBalance[] = [sampleWithRequiredData];
        expectedResult = service.addEventBalanceToCollectionIfMissing(eventBalanceCollection, ...eventBalanceArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const eventBalance: IEventBalance = sampleWithRequiredData;
        const eventBalance2: IEventBalance = sampleWithPartialData;
        expectedResult = service.addEventBalanceToCollectionIfMissing([], eventBalance, eventBalance2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(eventBalance);
        expect(expectedResult).toContain(eventBalance2);
      });

      it('should accept null and undefined values', () => {
        const eventBalance: IEventBalance = sampleWithRequiredData;
        expectedResult = service.addEventBalanceToCollectionIfMissing([], null, eventBalance, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(eventBalance);
      });

      it('should return initial array if no EventBalance is added', () => {
        const eventBalanceCollection: IEventBalance[] = [sampleWithRequiredData];
        expectedResult = service.addEventBalanceToCollectionIfMissing(eventBalanceCollection, undefined, null);
        expect(expectedResult).toEqual(eventBalanceCollection);
      });
    });

    describe('compareEventBalance', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareEventBalance(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 26921 };
        const entity2 = null;

        const compareResult1 = service.compareEventBalance(entity1, entity2);
        const compareResult2 = service.compareEventBalance(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 26921 };
        const entity2 = { id: 3958 };

        const compareResult1 = service.compareEventBalance(entity1, entity2);
        const compareResult2 = service.compareEventBalance(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 26921 };
        const entity2 = { id: 26921 };

        const compareResult1 = service.compareEventBalance(entity1, entity2);
        const compareResult2 = service.compareEventBalance(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
