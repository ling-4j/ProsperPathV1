import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../bill-participant.test-samples';

import { BillParticipantFormService } from './bill-participant-form.service';

describe('BillParticipant Form Service', () => {
  let service: BillParticipantFormService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BillParticipantFormService);
  });

  describe('Service methods', () => {
    describe('createBillParticipantFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createBillParticipantFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            shareAmount: expect.any(Object),
            bill: expect.any(Object),
            member: expect.any(Object),
          }),
        );
      });

      it('passing IBillParticipant should create a new form with FormGroup', () => {
        const formGroup = service.createBillParticipantFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            shareAmount: expect.any(Object),
            bill: expect.any(Object),
            member: expect.any(Object),
          }),
        );
      });
    });

    describe('getBillParticipant', () => {
      it('should return NewBillParticipant for default BillParticipant initial value', () => {
        const formGroup = service.createBillParticipantFormGroup(sampleWithNewData);

        const billParticipant = service.getBillParticipant(formGroup) as any;

        expect(billParticipant).toMatchObject(sampleWithNewData);
      });

      it('should return NewBillParticipant for empty BillParticipant initial value', () => {
        const formGroup = service.createBillParticipantFormGroup();

        const billParticipant = service.getBillParticipant(formGroup) as any;

        expect(billParticipant).toMatchObject({});
      });

      it('should return IBillParticipant', () => {
        const formGroup = service.createBillParticipantFormGroup(sampleWithRequiredData);

        const billParticipant = service.getBillParticipant(formGroup) as any;

        expect(billParticipant).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IBillParticipant should not enable id FormControl', () => {
        const formGroup = service.createBillParticipantFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewBillParticipant should disable id FormControl', () => {
        const formGroup = service.createBillParticipantFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
