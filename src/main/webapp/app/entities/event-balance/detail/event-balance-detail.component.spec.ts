import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { EventBalanceDetailComponent } from './event-balance-detail.component';

describe('EventBalance Management Detail Component', () => {
  let comp: EventBalanceDetailComponent;
  let fixture: ComponentFixture<EventBalanceDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EventBalanceDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./event-balance-detail.component').then(m => m.EventBalanceDetailComponent),
              resolve: { eventBalance: () => of({ id: 26921 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(EventBalanceDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EventBalanceDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('should load eventBalance on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', EventBalanceDetailComponent);

      // THEN
      expect(instance.eventBalance()).toEqual(expect.objectContaining({ id: 26921 }));
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
