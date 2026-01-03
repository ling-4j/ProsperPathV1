import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../bill.test-samples';

import { BillFormService } from './bill-form.service';

describe('Bill Form Service', () => {
  let service: BillFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BillFormService);
  });

  describe('Service methods', () => {
    describe('createBillFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createBillFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            amount: expect.any(Object),
            createdAt: expect.any(Object),
            event: expect.any(Object),
            payer: expect.any(Object),
          }),
        );
      });

      it('passing IBill should create a new form with FormGroup', () => {
        const formGroup = service.createBillFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            name: expect.any(Object),
            amount: expect.any(Object),
            createdAt: expect.any(Object),
            event: expect.any(Object),
            payer: expect.any(Object),
          }),
        );
      });
    });

    describe('getBill', () => {
      it('should return NewBill for default Bill initial value', () => {
        const formGroup = service.createBillFormGroup(sampleWithNewData);

        const bill = service.getBill(formGroup) as any;

        expect(bill).toMatchObject(sampleWithNewData);
      });

      it('should return NewBill for empty Bill initial value', () => {
        const formGroup = service.createBillFormGroup();

        const bill = service.getBill(formGroup) as any;

        expect(bill).toMatchObject({});
      });

      it('should return IBill', () => {
        const formGroup = service.createBillFormGroup(sampleWithRequiredData);

        const bill = service.getBill(formGroup) as any;

        expect(bill).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IBill should not enable id FormControl', () => {
        const formGroup = service.createBillFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewBill should disable id FormControl', () => {
        const formGroup = service.createBillFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
