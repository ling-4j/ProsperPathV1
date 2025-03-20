import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { ISummary } from '../summary.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../summary.test-samples';

import { RestSummary, SummaryService } from './summary.service';

const requireRestSample: RestSummary = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
  updatedAt: sampleWithRequiredData.updatedAt?.toJSON(),
};

describe('Summary Service', () => {
  let service: SummaryService;
  let httpMock: HttpTestingController;
  let expectedResult: ISummary | ISummary[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(SummaryService);
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

    it('should create a Summary', () => {
      const summary = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(summary).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Summary', () => {
      const summary = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(summary).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Summary', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Summary', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Summary', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addSummaryToCollectionIfMissing', () => {
      it('should add a Summary to an empty array', () => {
        const summary: ISummary = sampleWithRequiredData;
        expectedResult = service.addSummaryToCollectionIfMissing([], summary);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(summary);
      });

      it('should not add a Summary to an array that contains it', () => {
        const summary: ISummary = sampleWithRequiredData;
        const summaryCollection: ISummary[] = [
          {
            ...summary,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addSummaryToCollectionIfMissing(summaryCollection, summary);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Summary to an array that doesn't contain it", () => {
        const summary: ISummary = sampleWithRequiredData;
        const summaryCollection: ISummary[] = [sampleWithPartialData];
        expectedResult = service.addSummaryToCollectionIfMissing(summaryCollection, summary);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(summary);
      });

      it('should add only unique Summary to an array', () => {
        const summaryArray: ISummary[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const summaryCollection: ISummary[] = [sampleWithRequiredData];
        expectedResult = service.addSummaryToCollectionIfMissing(summaryCollection, ...summaryArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const summary: ISummary = sampleWithRequiredData;
        const summary2: ISummary = sampleWithPartialData;
        expectedResult = service.addSummaryToCollectionIfMissing([], summary, summary2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(summary);
        expect(expectedResult).toContain(summary2);
      });

      it('should accept null and undefined values', () => {
        const summary: ISummary = sampleWithRequiredData;
        expectedResult = service.addSummaryToCollectionIfMissing([], null, summary, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(summary);
      });

      it('should return initial array if no Summary is added', () => {
        const summaryCollection: ISummary[] = [sampleWithRequiredData];
        expectedResult = service.addSummaryToCollectionIfMissing(summaryCollection, undefined, null);
        expect(expectedResult).toEqual(summaryCollection);
      });
    });

    describe('compareSummary', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareSummary(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 9032 };
        const entity2 = null;

        const compareResult1 = service.compareSummary(entity1, entity2);
        const compareResult2 = service.compareSummary(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 9032 };
        const entity2 = { id: 17423 };

        const compareResult1 = service.compareSummary(entity1, entity2);
        const compareResult2 = service.compareSummary(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 9032 };
        const entity2 = { id: 9032 };

        const compareResult1 = service.compareSummary(entity1, entity2);
        const compareResult2 = service.compareSummary(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
