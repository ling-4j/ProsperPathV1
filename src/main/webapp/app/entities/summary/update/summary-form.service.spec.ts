import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../summary.test-samples';

import { SummaryFormService } from './summary-form.service';

describe('Summary Form Service', () => {
  let service: SummaryFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SummaryFormService);
  });

  describe('Service methods', () => {
    describe('createSummaryFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createSummaryFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            periodType: expect.any(Object),
            periodValue: expect.any(Object),
            totalAssets: expect.any(Object),
            totalIncome: expect.any(Object),
            totalExpense: expect.any(Object),
            totalProfit: expect.any(Object),
            profitPercentage: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });

      it('passing ISummary should create a new form with FormGroup', () => {
        const formGroup = service.createSummaryFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            periodType: expect.any(Object),
            periodValue: expect.any(Object),
            totalAssets: expect.any(Object),
            totalIncome: expect.any(Object),
            totalExpense: expect.any(Object),
            totalProfit: expect.any(Object),
            profitPercentage: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            user: expect.any(Object),
          }),
        );
      });
    });

    describe('getSummary', () => {
      it('should return NewSummary for default Summary initial value', () => {
        const formGroup = service.createSummaryFormGroup(sampleWithNewData);

        const summary = service.getSummary(formGroup) as any;

        expect(summary).toMatchObject(sampleWithNewData);
      });

      it('should return NewSummary for empty Summary initial value', () => {
        const formGroup = service.createSummaryFormGroup();

        const summary = service.getSummary(formGroup) as any;

        expect(summary).toMatchObject({});
      });

      it('should return ISummary', () => {
        const formGroup = service.createSummaryFormGroup(sampleWithRequiredData);

        const summary = service.getSummary(formGroup) as any;

        expect(summary).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ISummary should not enable id FormControl', () => {
        const formGroup = service.createSummaryFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewSummary should disable id FormControl', () => {
        const formGroup = service.createSummaryFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
