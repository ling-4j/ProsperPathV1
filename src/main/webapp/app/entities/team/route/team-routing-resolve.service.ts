import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ITeam } from '../team.model';
import { TeamService } from '../service/team.service';

const teamResolve = (route: ActivatedRouteSnapshot): Observable<null | ITeam> => {
  const id = route.params.id;
  if (id) {
    return inject(TeamService)
      .find(id)
      .pipe(
        mergeMap((team: HttpResponse<ITeam>) => {
          if (team.body) {
            return of(team.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default teamResolve;
