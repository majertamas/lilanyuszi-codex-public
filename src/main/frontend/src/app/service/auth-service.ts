import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {tap} from 'rxjs';
import {UserService} from './user-service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  http = inject(HttpClient);
  userService = inject(UserService);


  logout() {
    return this.http.post('/auth/logout', {}).pipe(
      tap(() => {
        this.userService.user.set(null);
      })
    );
  }

  refresh() {
    return this.http.post('/auth/refresh', {});
  }

}
