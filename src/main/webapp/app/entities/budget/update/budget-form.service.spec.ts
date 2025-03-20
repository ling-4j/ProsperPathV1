import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../budget.test-samples';

import { BudgetFormService } from './budget-form.service';

describe('Budget Form Service', () => {
  let service: BudgetFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BudgetFormService);
  });

  describe('Service methods', () => {
    describe('createBudgetFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createBudgetFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            budgetAmount: expect.any(Object),
            startDate: expect.any(Object),
            endDate: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            category: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });

      it('passing IBudget should create a new form with FormGroup', () => {
        const formGroup = service.createBudgetFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            budgetAmount: expect.any(Object),
            startDate: expect.any(Object),
            endDate: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            category: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });
    });

    describe('getBudget', () => {
      it('should return NewBudget for default Budget initial value', () => {
        const formGroup = service.createBudgetFormGroup(sampleWithNewData);

        const budget = service.getBudget(formGroup) as any;

        expect(budget).toMatchObject(sampleWithNewData);
      });

      it('should return NewBudget for empty Budget initial value', () => {
        const formGroup = service.createBudgetFormGroup();

        const budget = service.getBudget(formGroup) as any;

        expect(budget).toMatchObject({});
      });

      it('should return IBudget', () => {
        const formGroup = service.createBudgetFormGroup(sampleWithRequiredData);

        const budget = service.getBudget(formGroup) as any;

        expect(budget).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IBudget should not enable id FormControl', () => {
        const formGroup = service.createBudgetFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewBudget should disable id FormControl', () => {
        const formGroup = service.createBudgetFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
