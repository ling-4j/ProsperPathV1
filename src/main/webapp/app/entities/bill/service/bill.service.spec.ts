import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IBill } from '../bill.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../bill.test-samples';

import { BillService, RestBill } from './bill.service';

const requireRestSample: RestBill = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
};

describe('Bill Service', () => {
  let service: BillService;
  let httpMock: HttpTestingController;
  let expectedResult: IBill | IBill[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(BillService);
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

    it('should create a Bill', () => {
      const bill = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(bill).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Bill', () => {
      const bill = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(bill).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Bill', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Bill', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Bill', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addBillToCollectionIfMissing', () => {
      it('should add a Bill to an empty array', () => {
        const bill: IBill = sampleWithRequiredData;
        expectedResult = service.addBillToCollectionIfMissing([], bill);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(bill);
      });

      it('should not add a Bill to an array that contains it', () => {
        const bill: IBill = sampleWithRequiredData;
        const billCollection: IBill[] = [
          {
            ...bill,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addBillToCollectionIfMissing(billCollection, bill);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Bill to an array that doesn't contain it", () => {
        const bill: IBill = sampleWithRequiredData;
        const billCollection: IBill[] = [sampleWithPartialData];
        expectedResult = service.addBillToCollectionIfMissing(billCollection, bill);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(bill);
      });

      it('should add only unique Bill to an array', () => {
        const billArray: IBill[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const billCollection: IBill[] = [sampleWithRequiredData];
        expectedResult = service.addBillToCollectionIfMissing(billCollection, ...billArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const bill: IBill = sampleWithRequiredData;
        const bill2: IBill = sampleWithPartialData;
        expectedResult = service.addBillToCollectionIfMissing([], bill, bill2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(bill);
        expect(expectedResult).toContain(bill2);
      });

      it('should accept null and undefined values', () => {
        const bill: IBill = sampleWithRequiredData;
        expectedResult = service.addBillToCollectionIfMissing([], null, bill, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(bill);
      });

      it('should return initial array if no Bill is added', () => {
        const billCollection: IBill[] = [sampleWithRequiredData];
        expectedResult = service.addBillToCollectionIfMissing(billCollection, undefined, null);
        expect(expectedResult).toEqual(billCollection);
      });
    });

    describe('compareBill', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareBill(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 8530 };
        const entity2 = null;

        const compareResult1 = service.compareBill(entity1, entity2);
        const compareResult2 = service.compareBill(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 8530 };
        const entity2 = { id: 14455 };

        const compareResult1 = service.compareBill(entity1, entity2);
        const compareResult2 = service.compareBill(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 8530 };
        const entity2 = { id: 8530 };

        const compareResult1 = service.compareBill(entity1, entity2);
        const compareResult2 = service.compareBill(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
