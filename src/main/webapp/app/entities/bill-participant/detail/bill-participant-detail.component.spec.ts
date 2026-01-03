import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { BillParticipantDetailComponent } from './bill-participant-detail.component';

describe('BillParticipant Management Detail Component', () => {
  let comp: BillParticipantDetailComponent;
  let fixture: ComponentFixture<BillParticipantDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BillParticipantDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./bill-participant-detail.component').then(m => m.BillParticipantDetailComponent),
              resolve: { billParticipant: () => of({ id: 5254 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(BillParticipantDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BillParticipantDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load billParticipant on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', BillParticipantDetailComponent);

      // THEN
      expect(instance.billParticipant()).toEqual(expect.objectContaining({ id: 5254 }));
    });
  });

  describe('PreviousState', () => {
    it('should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
