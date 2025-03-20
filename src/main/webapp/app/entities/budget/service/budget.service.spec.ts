import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';

import { IBudget } from '../budget.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../budget.test-samples';

import { BudgetService, RestBudget } from './budget.service';

const requireRestSample: RestBudget = {
  ...sampleWithRequiredData,
  startDate: sampleWithRequiredData.startDate?.toJSON(),
  endDate: sampleWithRequiredData.endDate?.toJSON(),
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
  updatedAt: sampleWithRequiredData.updatedAt?.toJSON(),
};

describe('Budget Service', () => {
  let service: BudgetService;
  let httpMock: HttpTestingController;
  let expectedResult: IBudget | IBudget[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(BudgetService);
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

    it('should create a Budget', () => {
      const budget = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(budget).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Budget', () => {
      const budget = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(budget).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Budget', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Budget', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Budget', () => {
      const expected = true;

      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult).toBe(expected);
    });

    describe('addBudgetToCollectionIfMissing', () => {
      it('should add a Budget to an empty array', () => {
        const budget: IBudget = sampleWithRequiredData;
        expectedResult = service.addBudgetToCollectionIfMissing([], budget);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(budget);
      });

      it('should not add a Budget to an array that contains it', () => {
        const budget: IBudget = sampleWithRequiredData;
        const budgetCollection: IBudget[] = [
          {
            ...budget,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addBudgetToCollectionIfMissing(budgetCollection, budget);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Budget to an array that doesn't contain it", () => {
        const budget: IBudget = sampleWithRequiredData;
        const budgetCollection: IBudget[] = [sampleWithPartialData];
        expectedResult = service.addBudgetToCollectionIfMissing(budgetCollection, budget);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(budget);
      });

      it('should add only unique Budget to an array', () => {
        const budgetArray: IBudget[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const budgetCollection: IBudget[] = [sampleWithRequiredData];
        expectedResult = service.addBudgetToCollectionIfMissing(budgetCollection, ...budgetArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const budget: IBudget = sampleWithRequiredData;
        const budget2: IBudget = sampleWithPartialData;
        expectedResult = service.addBudgetToCollectionIfMissing([], budget, budget2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(budget);
        expect(expectedResult).toContain(budget2);
      });

      it('should accept null and undefined values', () => {
        const budget: IBudget = sampleWithRequiredData;
        expectedResult = service.addBudgetToCollectionIfMissing([], null, budget, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(budget);
      });

      it('should return initial array if no Budget is added', () => {
        const budgetCollection: IBudget[] = [sampleWithRequiredData];
        expectedResult = service.addBudgetToCollectionIfMissing(budgetCollection, undefined, null);
        expect(expectedResult).toEqual(budgetCollection);
      });
    });

    describe('compareBudget', () => {
      it('Should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareBudget(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('Should return false if one entity is null', () => {
        const entity1 = { id: 5320 };
        const entity2 = null;

        const compareResult1 = service.compareBudget(entity1, entity2);
        const compareResult2 = service.compareBudget(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey differs', () => {
        const entity1 = { id: 5320 };
        const entity2 = { id: 31697 };

        const compareResult1 = service.compareBudget(entity1, entity2);
        const compareResult2 = service.compareBudget(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('Should return false if primaryKey matches', () => {
        const entity1 = { id: 5320 };
        const entity2 = { id: 5320 };

        const compareResult1 = service.compareBudget(entity1, entity2);
        const compareResult2 = service.compareBudget(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
