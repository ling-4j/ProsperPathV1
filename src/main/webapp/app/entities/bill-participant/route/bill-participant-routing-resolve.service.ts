import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IBillParticipant } from '../bill-participant.model';
import { BillParticipantService } from '../service/bill-participant.service';

const billParticipantResolve = (route: ActivatedRouteSnapshot): Observable<null | IBillParticipant> => {
  const id = route.params.id;
  if (id) {
    return inject(BillParticipantService)
      .find(id)
      .pipe(
        mergeMap((billParticipant: HttpResponse<IBillParticipant>) => {
          if (billParticipant.body) {
            return of(billParticipant.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default billParticipantResolve;
