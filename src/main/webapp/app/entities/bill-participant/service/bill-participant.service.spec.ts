import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IBillParticipant } from '../bill-participant.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../bill-participant.test-samples';

import { BillParticipantService } from './bill-participant.service';

const requireRestSample: IBillParticipant = {
  ...sampleWithRequiredData,
};

describe('BillParticipant Service', () => {
  let service: BillParticipantService;
  let httpMock: HttpTestingController;
  let expectedResult: IBillParticipant | IBillParticipant[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(BillParticipantService);
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

    it('should create a BillParticipant', () => {
      const billParticipant = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(billParticipant).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a BillParticipant', () => {
      const billParticipant = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(billParticipant).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a BillParticipant', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of BillParticipant', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a BillParticipant', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addBillParticipantToCollectionIfMissing', () => {
      it('should add a BillParticipant to an empty array', () => {
        const billParticipant: IBillParticipant = sampleWithRequiredData;
        expectedResult = service.addBillParticipantToCollectionIfMissing([], billParticipant);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(billParticipant);
      });

      it('should not add a BillParticipant to an array that contains it', () => {
        const billParticipant: IBillParticipant = sampleWithRequiredData;
        const billParticipantCollection: IBillParticipant[] = [
          {
            ...billParticipant,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addBillParticipantToCollectionIfMissing(billParticipantCollection, billParticipant);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a BillParticipant to an array that doesn't contain it", () => {
        const billParticipant: IBillParticipant = sampleWithRequiredData;
        const billParticipantCollection: IBillParticipant[] = [sampleWithPartialData];
        expectedResult = service.addBillParticipantToCollectionIfMissing(billParticipantCollection, billParticipant);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(billParticipant);
      });

      it('should add only unique BillParticipant to an array', () => {
        const billParticipantArray: IBillParticipant[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const billParticipantCollection: IBillParticipant[] = [sampleWithRequiredData];
        expectedResult = service.addBillParticipantToCollectionIfMissing(billParticipantCollection, ...billParticipantArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const billParticipant: IBillParticipant = sampleWithRequiredData;
        const billParticipant2: IBillParticipant = sampleWithPartialData;
        expectedResult = service.addBillParticipantToCollectionIfMissing([], billParticipant, billParticipant2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(billParticipant);
        expect(expectedResult).toContain(billParticipant2);
      });

      it('should accept null and undefined values', () => {
        const billParticipant: IBillParticipant = sampleWithRequiredData;
        expectedResult = service.addBillParticipantToCollectionIfMissing([], null, billParticipant, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(billParticipant);
      });

      it('should return initial array if no BillParticipant is added', () => {
        const billParticipantCollection: IBillParticipant[] = [sampleWithRequiredData];
        expectedResult = service.addBillParticipantToCollectionIfMissing(billParticipantCollection, undefined, null);
        expect(expectedResult).toEqual(billParticipantCollection);
      });
    });

    describe('compareBillParticipant', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareBillParticipant(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 5254 };
        const entity2 = null;

        const compareResult1 = service.compareBillParticipant(entity1, entity2);
        const compareResult2 = service.compareBillParticipant(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 5254 };
        const entity2 = { id: 937 };

        const compareResult1 = service.compareBillParticipant(entity1, entity2);
        const compareResult2 = service.compareBillParticipant(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 5254 };
        const entity2 = { id: 5254 };

        const compareResult1 = service.compareBillParticipant(entity1, entity2);
        const compareResult2 = service.compareBillParticipant(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
