import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';

import { SummaryDetailComponent } from './summary-detail.component';

describe('Summary Management Detail Component', () => {
  let comp: SummaryDetailComponent;
  let fixture: ComponentFixture<SummaryDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SummaryDetailComponent],
      providers: [
        provideRouter(
          [
            {
              path: '**',
              loadComponent: () => import('./summary-detail.component').then(m => m.SummaryDetailComponent),
              resolve: { summary: () => of({ id: 9032 }) },
            },
          ],
          withComponentInputBinding(),
        ),
      ],
    })
      .overrideTemplate(SummaryDetailComponent, '')
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SummaryDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load summary on init', async () => {
      const harness = await RouterTestingHarness.create();
      const instance = await harness.navigateByUrl('/', SummaryDetailComponent);

      // THEN
      expect(instance.summary()).toEqual(expect.objectContaining({ id: 9032 }));
    });
  });

  describe('PreviousState', () => {
    it('Should navigate to previous state', () => {
      jest.spyOn(window.history, 'back');
      comp.previousState();
      expect(window.history.back).toHaveBeenCalled();
    });
  });
});
